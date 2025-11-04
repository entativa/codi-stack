package io.codibase.server.event.project.pullrequest;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.BuildService;
import io.codibase.server.model.Build;

public class PullRequestBuildEvent extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long buildId;
	
	public PullRequestBuildEvent(Build build) {
		super(null, new Date(), build.getRequest());
		buildId = build.getId();
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	public Build getBuild() {
		return CodiBase.getInstance(BuildService.class).load(buildId);
	}

	@Override
	public String getActivity() {
		Build build = getBuild();
		String activity = build.getJobName() + " ";
		if (build.getVersion() != null)
			activity = "build #" + build.getNumber() + " (" + build.getVersion() + ")";
		else
			activity = "build #" + build.getNumber();
		activity += " is " + build.getStatus();
		return activity;
	}

}
