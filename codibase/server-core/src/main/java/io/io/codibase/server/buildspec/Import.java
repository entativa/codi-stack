package io.codibase.server.buildspec;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;

import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.Collections;
import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.ClassValidating;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.service.ProjectService;
import io.codibase.server.job.JobAuthorizationContext;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.security.permission.AccessProject;
import io.codibase.server.security.permission.ProjectPermission;
import io.codibase.server.security.permission.ReadCode;
import io.codibase.server.util.EditContext;
import io.codibase.server.util.facade.ProjectCache;
import io.codibase.server.validation.Validatable;
import io.codibase.server.web.page.project.ProjectPage;
import io.codibase.server.web.util.SuggestionUtils;
import io.codibase.server.web.util.WicketUtils;

@Editable
@ClassValidating
public class Import implements Serializable, Validatable {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(Import.class);

	private String projectPath;
	
	private String revision;
	
	private String accessTokenSecret;
	
	private transient Project project;
	
	private transient RevCommit commit;
	
	private transient BuildSpec buildSpec;
	
	private static ThreadLocal<Stack<String>> IMPORT_CHAIN = ThreadLocal.withInitial(Stack::new);
	
	// change Named("projectPath") also if change name of this property 
	@Editable(order=100, name="Project", description="Specify project to import build spec from")
	@ChoiceProvider("getProjectChoices")
	@NotEmpty
	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	@SuppressWarnings("unused")
	private static List<String> getProjectChoices() {
		ProjectService projectService = CodiBase.getInstance(ProjectService.class);
		Project project = ((ProjectPage)WicketUtils.getPage()).getProject();
		
		Collection<Project> projects = SecurityUtils.getAuthorizedProjects(new AccessProject());
		projects.remove(project);
		
		ProjectCache cache = projectService.cloneCache();

		List<String> choices = projects.stream().map(it->cache.get(it.getId()).getPath()).collect(Collectors.toList());
		Collections.sort(choices);
		
		return choices;
	}
	
	@Nullable
	private static Project getInputProject() {
		String projectPath = (String) EditContext.get().getInputValue("projectPath");
		if (projectPath != null) {
			Project project = CodiBase.getInstance(ProjectService.class).findByPath(projectPath);
			if (project != null && SecurityUtils.canReadCode(project))
				return project;
		}
		return null;
	}
	
	@Editable(order=200, description="Specify branch, tag or commit in above project to import " +
			"build spec from")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestRevisions")
	@NotEmpty
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRevisions(String matchWith) {
		Project project = getInputProject();
		if (project != null)
			return SuggestionUtils.suggestRevisions(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=500, description="Specify a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token to import build spec " +
			"from above project if its code is not publicly accessible")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@Nullable
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}
	
	public BuildSpec getBuildSpec() {
		if (buildSpec == null) {
			Project project = getProject();

			Subject subject;
			try {
				subject = JobAuthorizationContext.get().getSubject(getAccessTokenSecret());
			} catch (ExplicitException e) {
				var errorMessage = MessageFormat.format(
						_T("Unable to import build spec (import project: {0}, import revision: {1}): {2}"),
						projectPath, revision, e.getMessage());
				throw new ExplicitException(errorMessage);
			}
			if (!subject.isPermitted(new ProjectPermission(project, new ReadCode())) 
					&& !project.isPermittedByLoginUser(new ReadCode())) {
				String errorMessage = MessageFormat.format(
						_T("Code read permission is required to import build spec (import project: {0}, import revision: {1})"), 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			
			RevCommit commit = getCommit();
			try {
				buildSpec = project.getBuildSpec(commit);
			} catch (BuildSpecParseException e) {
				String errorMessage = MessageFormat.format(
						_T("Malformed build spec (import project: {0}, import revision: {1})"), 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			if (buildSpec == null) {
				String errorMessage = MessageFormat.format(
						_T("Build spec not defined (import project: {0}, import revision: {1})"), 
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
			
		}
		return buildSpec;
	}
	
	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		try {
			var commit = getCommit();
			if (IMPORT_CHAIN.get().contains(commit.name())) {
				List<String> circular = new ArrayList<>(IMPORT_CHAIN.get());
				circular.add(commit.name());
				String errorMessage = MessageFormat.format(
						_T("Circular build spec imports ({0})"), circular);
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
				return false;
			} else {
				IMPORT_CHAIN.get().push(commit.name());
				try {
					Validator validator = CodiBase.getInstance(Validator.class);
					BuildSpec buildSpec = getBuildSpec();
					JobAuthorizationContext.push(new JobAuthorizationContext(getProject(), getCommit(), null));
					try {
						for (int i = 0; i < buildSpec.getImports().size(); i++) {
							Import aImport = buildSpec.getImports().get(i);
							for (ConstraintViolation<Import> violation : validator.validate(aImport)) {
								String location = "imports[" + i + "]";
								if (violation.getPropertyPath().toString().length() != 0)
									location += "." + violation.getPropertyPath();
								String message = String.format("Error validating imported build spec (import project: %s, import revision: %s, location: %s, message: %s)",
										projectPath, revision, location, violation.getMessage());
								throw new ValidationException(message);
							}
						}
						return true;
					} finally {
						JobAuthorizationContext.pop();
					}
				} finally {
					IMPORT_CHAIN.get().pop();
				}
			}
		} catch (Exception e) {
			context.disableDefaultConstraintViolation();
			String message = e.getMessage();
			if (message == null) {
				logger.error("Error validating build spec import", e);
				message = _T("Failed to validate build spec import. Check server log for details");
			}
			context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
			return false;
		}
	}
	
	public Project getProject() {
		if (project == null) {
			project = CodiBase.getInstance(ProjectService.class).findByPath(projectPath);
			if (project == null)
				throw new ExplicitException(MessageFormat.format( _T("Unable to find project to import build spec: {0}"), projectPath));
		}
		return project;
	}
	
	public RevCommit getCommit() {
		if (commit == null) {
			commit = getProject().getRevCommit(revision, false);
			if (commit == null) {
				String errorMessage = MessageFormat.format(
						_T("Unable to find commit to import build spec (import project: {0}, import revision: {1})"),
						projectPath, revision);
				throw new ExplicitException(errorMessage);
			}
		}
		return commit;
	}
	
}
