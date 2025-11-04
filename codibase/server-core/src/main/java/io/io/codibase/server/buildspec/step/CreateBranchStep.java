package io.codibase.server.buildspec.step;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.BranchName;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.service.BuildService;
import io.codibase.server.git.GitUtils;
import io.codibase.server.git.service.GitService;
import io.codibase.server.git.service.RefFacade;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.web.util.SuggestionUtils;
import org.eclipse.jgit.lib.Repository;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Editable(name="Create Branch", order=280)
public class CreateBranchStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String branchName;
	
	private String branchRevision;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the branch")
	@Interpolative(variableSuggester="suggestVariables")
	@BranchName
	@NotEmpty
	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	@Editable(order=1100, placeholder = "Build Commit", description="Optionally specify revision " +
			"to create branch from. Leave empty to create from build commit")
	@Interpolative(variableSuggester="suggestVariables", literalSuggester="suggestRevisions")
	@NotEmpty
	public String getBranchRevision() {
		return branchRevision;
	}

	public void setBranchRevision(String branchRevision) {
		this.branchRevision = branchRevision;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRevisions(String matchWith) {
		Project project = Project.get();
		if (project != null)
			return SuggestionUtils.suggestRevisions(project, matchWith);
		else
			return new ArrayList<>();
	}

	@Editable(order=1060, description="For build commit not reachable from default branch, " +
			"a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with create branch permission")
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
			Project project = build.getProject();
			String branchName = getBranchName();

			if (!Repository.isValidRefName(GitUtils.branch2ref(branchName)))
				throw new ExplicitException("Invalid branch name: " + branchName);

			if (build.canCreateBranch(getAccessTokenSecret(), branchName)) {
				RefFacade branchRef = project.getBranchRef(branchName);
				if (branchRef != null) {
					logger.warning("Branch '" + branchName + "' already exists");
				} else {
					String branchRevision = getBranchRevision();
					if (branchRevision == null)
						branchRevision = build.getCommitHash();
					CodiBase.getInstance(GitService.class).createBranch(project, branchName, branchRevision);
				}
			} else {
				logger.error("This build is not authorized to create branch '" + branchName + "'");
				return new ServerStepResult(false);
			}
			return new ServerStepResult(true);
		});
	}

}
