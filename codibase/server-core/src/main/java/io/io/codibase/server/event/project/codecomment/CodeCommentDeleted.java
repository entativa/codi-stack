package io.codibase.server.event.project.codecomment;

import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.CodeComment;
import io.codibase.server.security.SecurityUtils;

import java.util.Date;

public class CodeCommentDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	public CodeCommentDeleted(CodeComment comment) {
		super(SecurityUtils.getUser(), new Date(), comment.getProject());
		commentId = comment.getId();
	}

	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getActivity() {
		return "code comment deleted";
	}
	
}
