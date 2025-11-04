package io.codibase.server.event.project.issue;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueCommentService;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.IssueComment;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

import java.util.Collection;

public class IssueCommentCreated extends IssueEvent {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public IssueCommentCreated(IssueComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getIssue());
		commentId = comment.getId();
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public IssueComment getComment() {
		return CodiBase.getInstance(IssueCommentService.class).load(commentId);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public boolean affectsListing() {
		return false;
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	public String getActivity() {
		return "commented";
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getComment(), true);
	}

}
