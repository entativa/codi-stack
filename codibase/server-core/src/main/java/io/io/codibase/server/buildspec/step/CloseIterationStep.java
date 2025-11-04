package io.codibase.server.buildspec.step;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.IterationService;
import io.codibase.server.model.Iteration;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Editable(name="Close Iteration", order=400)
public class CloseIterationStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String iterationName;
	
	private String accessTokenSecret;
	
	@Editable(order=1000, description="Specify name of the iteration")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getIterationName() {
		return iterationName;
	}

	public void setIterationName(String iterationName) {
		this.iterationName = iterationName;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Editable(order=1060, description="For build commit not reachable from default branch, " +
			"a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> should be specified as access token with manage issue permission")
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
		return CodiBase.getInstance(TransactionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			Project project = build.getProject();
			String iterationName = getIterationName();
			IterationService iterationService = CodiBase.getInstance(IterationService.class);
			Iteration iteration = iterationService.findInHierarchy(project, iterationName);
			if (iteration != null) {
				if (build.canCloseIteration(getAccessTokenSecret())) {
					iteration.setClosed(true);
					iterationService.createOrUpdate(iteration);
				} else {
					logger.error("This build is not authorized to close iteration '" + iterationName + "'");
					return new ServerStepResult(false);
				}
			} else {
				logger.warning("Unable to find iteration '" + iterationName + "' to close. Ignored.");
			}
			return new ServerStepResult(true);
		});
	}

}
