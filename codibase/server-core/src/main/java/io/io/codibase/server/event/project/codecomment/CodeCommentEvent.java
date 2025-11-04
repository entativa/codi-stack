package io.codibase.server.event.project.codecomment;

import java.util.Date;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.web.UrlService;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.User;

public abstract class CodeCommentEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long commentId;
	
	/**
	 * @param comment
	 * @param user
	 * @param date
	 */
	public CodeCommentEvent(User user, Date date, CodeComment comment) {
		super(user, date, comment.getProject());
		commentId = comment.getId();
	}

	public CodeComment getComment() {
		return CodiBase.getInstance(CodeCommentService.class).load(commentId);
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getComment(), true);
	}
	
}
