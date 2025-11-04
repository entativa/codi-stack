package io.codibase.server.buildspec.job.trigger;

import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.TriggerMatch;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.event.project.pullrequest.PullRequestChanged;
import io.codibase.server.model.support.pullrequest.changedata.PullRequestDiscardData;
import io.codibase.server.annotation.Editable;

@Editable(order=320, name="Pull request discard", description="Job will run on head commit of target branch")
public class PullRequestDiscardTrigger extends PullRequestTrigger {

	private static final long serialVersionUID = 1L;

	@Override
	protected TriggerMatch triggerMatches(ProjectEvent event, Job job) {
		if (event instanceof PullRequestChanged) {
			PullRequestChanged pullRequestChangeEvent = (PullRequestChanged) event;
			if (pullRequestChangeEvent.getChange().getData() instanceof PullRequestDiscardData)
				return triggerMatches(pullRequestChangeEvent.getRequest(), "Pull request is discarded");
		}
		return null;
	}

	@Override
	public String getTriggerDescription() {
		return getTriggerDescription("discard");
	}

}
