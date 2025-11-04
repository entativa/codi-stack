package io.codibase.server.buildspec.step;

import static io.codibase.server.buildspec.step.StepGroup.DOCKER_IMAGE;

import io.codibase.k8shelper.PruneBuilderCacheFacade;
import io.codibase.k8shelper.StepFacade;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.ReservedOptions;
import io.codibase.server.buildspec.param.ParamCombination;
import io.codibase.server.model.Build;
import io.codibase.server.model.support.administration.jobexecutor.DockerAware;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;
import io.codibase.server.model.support.administration.jobexecutor.KubernetesAware;

@Editable(order=260, name="Prune Builder Cache", group = DOCKER_IMAGE, description="" +
		"Prune image cache of docker buildx builder. This step calls docker builder prune command " +
		"to remove cache of buildx builder specified in server docker executor or remote docker executor")
public class PruneBuilderCacheStep extends Step {

	private static final long serialVersionUID = 1L;
	
	private String options;

	@Editable(order=100, description = "Optionally specify options for docker builder prune command")
	@ReservedOptions({"-f", "--force", "--builder"})
	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	@Override
	public StepFacade getFacade(Build build, JobExecutor jobExecutor, String jobToken, ParamCombination paramCombination) {
		return new PruneBuilderCacheFacade(getOptions());
	}
	
	@Override
	public boolean isApplicable(Build build, JobExecutor executor) {
		return executor instanceof DockerAware && !(executor instanceof KubernetesAware);
	}

}
