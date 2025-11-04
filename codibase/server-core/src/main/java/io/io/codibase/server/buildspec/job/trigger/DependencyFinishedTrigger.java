package io.codibase.server.buildspec.job.trigger;

import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.JobDependency;
import io.codibase.server.buildspec.job.TriggerMatch;
import io.codibase.server.buildspec.param.instance.ParamInstances;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.event.project.build.BuildFinished;
import io.codibase.server.model.Build;
import io.codibase.server.model.Build.Status;

import static io.codibase.server.buildspec.param.ParamUtils.isCoveredBy;
import static io.codibase.server.buildspec.param.ParamUtils.resolveParams;
import static java.util.stream.Collectors.toSet;

@Editable(order=500, name="Dependency job finished")
public class DependencyFinishedTrigger extends JobTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof BuildFinished) {
			BuildFinished buildFinished = (BuildFinished) event;
			Build build = buildFinished.getBuild();
			for (JobDependency dependency: job.getJobDependencies()) {
				if (dependency.getJobName().equals(build.getJobName()) 
						&& (!dependency.isRequireSuccessful() || build.getStatus() == Status.SUCCESSFUL)) {
					var secretParamNames = dependency.getParamMatrix().stream()
							.filter(ParamInstances::isSecret)
							.map(ParamInstances::getName)
							.collect(toSet());					
					for (var paramMap: resolveParams(null, null, 
							dependency.getParamMatrix(), dependency.getExcludeParamMaps())) {
						if (isCoveredBy(build.getParamMap(), paramMap, secretParamNames)) {
							return new TriggerMatch(build.getRefName(), build.getRequest(), build.getIssue(), getParamMatrix(),
									getExcludeParamMaps(), "Dependency job '" + dependency.getJobName() + "' is finished");
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return "When dependency jobs finished";
	}

}
