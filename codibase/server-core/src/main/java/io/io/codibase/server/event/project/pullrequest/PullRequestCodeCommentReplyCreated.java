package io.codibase.server.event.project.pullrequest;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentReplyService;
import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;
import io.codibase.server.web.UrlService;

public class PullRequestCodeCommentReplyCreated extends PullRequestCodeCommentEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long replyId;
	
	public PullRequestCodeCommentReplyCreated(PullRequest request, CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), request, reply.getComment());
		replyId = reply.getId();
	}

	public CodeCommentReply getReply() {
		return CodiBase.getInstance(CodeCommentReplyService.class).load(replyId);
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getReply().getContent());
	}

	@Override
	public String getActivity() {
		return "replied code comment"; 
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getReply(), true);
	}

}
