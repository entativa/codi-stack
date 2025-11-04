package io.codibase.server.event.project.pullrequest;

import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.web.UrlService;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;

public abstract class PullRequestEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long requestId;
	
	public PullRequestEvent(@Nullable User user, Date date, PullRequest request) {
		super(user, date, request.getTargetProject());
		requestId = request.getId();
	}

	public PullRequest getRequest() {
		return CodiBase.getInstance(PullRequestService.class).load(requestId);
	}

	@Override
	public String getLockName() {
		return PullRequest.getSerialLockName(requestId);
	}
	
	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getRequest(), true);
	}
	
}
