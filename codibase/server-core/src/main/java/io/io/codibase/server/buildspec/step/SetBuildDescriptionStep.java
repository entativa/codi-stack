package io.codibase.server.buildspec.step;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Markdown;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.service.BuildService;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.build.BuildUpdated;
import io.codibase.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.List;

@Editable(order=265, name="Set Build Description")
public class SetBuildDescriptionStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String buildDescription;

	@Editable(order=100)
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	@Markdown
	public String getBuildDescription() {
		return buildDescription;
	}

	public void setBuildDescription(String buildDescription) {
		this.buildDescription = buildDescription;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, true, false);
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger jobLogger) {
		return CodiBase.getInstance(TransactionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			build.setDescription(buildDescription);
			CodiBase.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
			return new ServerStepResult(true);
		});
		
	}

}
