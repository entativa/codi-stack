package io.codibase.server.buildspec.step;

import static io.codibase.server.buildspec.step.StepGroup.DOCKER_IMAGE;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.k8shelper.RunImagetoolsFacade;
import io.codibase.k8shelper.StepFacade;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.param.ParamCombination;
import io.codibase.server.model.Build;
import io.codibase.server.model.support.administration.jobexecutor.DockerAware;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;
import io.codibase.server.model.support.administration.jobexecutor.KubernetesAware;

@Editable(order=230, name="Run Buildx Image Tools", group = DOCKER_IMAGE, description="Run docker buildx imagetools " +
		"command with specified arguments. This step can only be executed by server docker executor " +
		"or remote docker executor")
public class RunImagetoolsStep extends Step {

	private static final long serialVersionUID = 1L;
	
	private String arguments;
	
	private List<RegistryLogin> registryLogins = new ArrayList<>();

	@Editable(order=100, description="Specify arguments for imagetools. For instance " +
			"<code>create -t myorg/myrepo:1.0.0 myorg/myrepo@&lt;arm64 manifest digest&gt; myorg/myrepo@&lt;amd64 manifest digest&gt;</code>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	@Editable(order=200, group="More Settings", description="Optionally specify registry logins to override " +
			"those defined in job executor. For built-in registry, use <code>@server_url@</code> for registry url, " +
			"<code>@job_token@</code> for user name, and access token secret for password secret")
	@Valid
	public List<RegistryLogin> getRegistryLogins() {
		return registryLogins;
	}

	public void setRegistryLogins(List<RegistryLogin> registryLogins) {
		this.registryLogins = registryLogins;
	}
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}
	
	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		var registryLogins = getRegistryLogins().stream().map(it->it.getFacade(build)).collect(toList());
		return new RunImagetoolsFacade(getArguments(), registryLogins);
	}

	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return executor instanceof DockerAware && !(executor instanceof KubernetesAware);
	}

}
