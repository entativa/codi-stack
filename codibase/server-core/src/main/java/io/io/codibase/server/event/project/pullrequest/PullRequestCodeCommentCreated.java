package io.codibase.server.event.project.pullrequest;

import io.codibase.server.CodiBase;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;
import io.codibase.server.web.UrlService;

public class PullRequestCodeCommentCreated extends PullRequestCodeCommentEvent {

	private static final long serialVersionUID = 1L;

	public PullRequestCodeCommentCreated(PullRequest request, CodeComment comment) {
		super(comment.getUser(), comment.getCreateDate(), request, comment);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public String getActivity() {
		return "created code comment"; 
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getComment(), true);
	}

}
