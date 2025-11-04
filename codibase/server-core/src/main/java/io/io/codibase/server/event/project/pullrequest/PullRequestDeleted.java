package io.codibase.server.event.project.pullrequest;

import java.util.Date;

import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.PullRequest;
import io.codibase.server.security.SecurityUtils;

public class PullRequestDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestDeleted(PullRequest request) {
		super(SecurityUtils.getUser(), new Date(), request.getProject());
		requestId = request.getId();
	}

	public Long getRequestId() {
		return requestId;
	}

	@Override
	public String getActivity() {
		return "pull request deleted";
	}
	
}
