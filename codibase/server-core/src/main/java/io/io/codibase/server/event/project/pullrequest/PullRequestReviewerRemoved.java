package io.codibase.server.event.project.pullrequest;

import java.text.MessageFormat;
import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.UserService;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;

public class PullRequestReviewerRemoved extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long reviewerId;
	
	public PullRequestReviewerRemoved(User user, Date date, PullRequest request, User reviewer) {
		super(user, date, request);
		reviewerId = reviewer.getId();
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public String getActivity() {
		return MessageFormat.format("removed reviewer \"{0}\"", getReviewer().getDisplayName());
	}

	public User getReviewer() {
		return CodiBase.getInstance(UserService.class).load(reviewerId);
	}

}
