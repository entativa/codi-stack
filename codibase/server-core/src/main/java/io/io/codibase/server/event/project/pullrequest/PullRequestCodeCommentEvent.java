package io.codibase.server.event.project.pullrequest;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;

public abstract class PullRequestCodeCommentEvent extends PullRequestEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public PullRequestCodeCommentEvent(User user, Date date, PullRequest request, CodeComment comment) {
		super(user, date, request);
		commentId = comment.getId();
	}

	public CodeComment getComment() {
		return CodiBase.getInstance(CodeCommentService.class).load(commentId);
	}

}
