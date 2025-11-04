package io.codibase.server.event.project.issue;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueCommentService;
import io.codibase.server.model.IssueComment;
import io.codibase.server.security.SecurityUtils;

import java.util.Date;

public class IssueCommentEdited extends IssueEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public IssueCommentEdited(IssueComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getIssue());
		this.commentId = comment.getId();
	}
	
	public IssueComment getComment() {
		return CodiBase.getInstance(IssueCommentService.class).load(commentId);
	}
	
	@Override
	public boolean affectsListing() {
		return false;
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
