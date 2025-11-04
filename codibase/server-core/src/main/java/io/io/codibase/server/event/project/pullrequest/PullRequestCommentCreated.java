package io.codibase.server.event.project.pullrequest;

import java.util.Collection;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestCommentService;
import io.codibase.server.model.PullRequestComment;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;
import io.codibase.server.web.UrlService;

public class PullRequestCommentCreated extends PullRequestEvent {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	private final Collection<String> notifiedEmailAddresses;
	
	public PullRequestCommentCreated(PullRequestComment comment, Collection<String> notifiedEmailAddresses) {
		super(comment.getUser(), comment.getDate(), comment.getRequest());
		commentId = comment.getId();
		this.notifiedEmailAddresses = notifiedEmailAddresses;
	}

	public PullRequestComment getComment() {
		return CodiBase.getInstance(PullRequestCommentService.class).load(commentId);
	}

	public Collection<String> getNotifiedEmailAddresses() {
		return notifiedEmailAddresses;
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
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
