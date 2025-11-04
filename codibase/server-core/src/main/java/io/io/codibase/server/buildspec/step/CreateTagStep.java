package io.codibase.server.buildspec.step;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.*;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.service.UserService;
import io.codibase.server.git.GitUtils;
import io.codibase.server.git.service.GitService;
import io.codibase.server.git.service.RefFacade;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.SessionService;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Editable(name="Create Tag", order=300)
public class CreateTagStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String tagName;
	
	private String tagMessage;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the tag")
	@Interpolative(variableSuggester="suggestVariables")
	@TagName
	@NotEmpty
	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	
	@Editable(order=1050, description="Optionally specify message of the tag")
	@Multiline
	@Interpolative(variableSuggester="suggestVariables")
	public String getTagMessage() {
		return tagMessage;
	}

	public void setTagMessage(String tagMessage) {
		this.tagMessage = tagMessage;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Editable(order=1060, description="For build commit not reachable from default branch, " +
			"a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create tag permission")
	@ChoiceProvider("getAccessTokenSecretChoices")
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

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(SessionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			PersonIdent taggerIdent = CodiBase.getInstance(UserService.class).getSystem().asPerson();
			Project project = build.getProject();
			String tagName = getTagName();

			if (!Repository.isValidRefName(GitUtils.tag2ref(tagName))) {
				logger.error("Invalid tag name: " + tagName);
				return new ServerStepResult(false);
			}

			if (build.canCreateTag(getAccessTokenSecret(), tagName)) {
				RefFacade tagRef = project.getTagRef(tagName);
				if (tagRef != null)
					CodiBase.getInstance(ProjectService.class).deleteTag(project, tagName);
				CodiBase.getInstance(GitService.class).createTag(project, tagName, build.getCommitHash(),
						taggerIdent, getTagMessage(), false);
			} else {
				throw new ExplicitException("This build is not authorized to create tag '" + tagName + "'");
			}
			return new ServerStepResult(true);
		});
	}

}
