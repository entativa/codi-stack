package io.codibase.server.buildspec.step;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.service.BuildService;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.build.BuildUpdated;
import io.codibase.server.persistence.TransactionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.codibase.k8shelper.KubernetesHelper.BUILD_VERSION;

@Editable(order=260, name="Set Build Version")
public class SetBuildVersionStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;

	private String buildVersion;

	@Editable(order=100)
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, true, false);
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(TransactionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			build.setVersion(buildVersion);
			CodiBase.getInstance(ListenerRegistry.class).post(new BuildUpdated(build));
			Map<String, byte[]> outputFiles = new HashMap<>();
			outputFiles.put(BUILD_VERSION, buildVersion.getBytes(StandardCharsets.UTF_8));
			return new ServerStepResult(true, outputFiles);
		});
		
	}

}
