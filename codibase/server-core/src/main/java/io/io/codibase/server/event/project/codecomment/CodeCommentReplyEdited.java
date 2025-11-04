package io.codibase.server.event.project.codecomment;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentReplyService;
import io.codibase.server.model.CodeCommentReply;
import io.codibase.server.security.SecurityUtils;

import java.util.Date;

public class CodeCommentReplyEdited extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long replyId;
	
	public CodeCommentReplyEdited(CodeCommentReply reply) {
		super(SecurityUtils.getAuthUser(), new Date(), reply.getComment());
		this.replyId = reply.getId();
	}
	
	public CodeCommentReply getReply() {
		return CodiBase.getInstance(CodeCommentReplyService.class).load(replyId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public String getActivity() {
		return "reply edited";
	}

}
