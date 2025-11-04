package io.codibase.server.event.project.pullrequest;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestCommentService;
import io.codibase.server.model.PullRequestComment;
import io.codibase.server.security.SecurityUtils;

public class PullRequestCommentEdited extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public PullRequestCommentEdited(PullRequestComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getRequest());
		this.commentId = comment.getId();
	}
	
	public PullRequestComment getComment() {
		return CodiBase.getInstance(PullRequestCommentService.class).load(commentId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "comment edited";
	}

}
