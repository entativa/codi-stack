package io.codibase.server.event.project.codecomment;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentReplyService;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

public class CodeCommentReplyCreated extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;

	private final Long replyId;
	
	public CodeCommentReplyCreated(CodeCommentReply reply) {
		super(reply.getUser(), reply.getDate(), reply.getComment());
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
		return "replied";
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getReply(), true);
	}

}
