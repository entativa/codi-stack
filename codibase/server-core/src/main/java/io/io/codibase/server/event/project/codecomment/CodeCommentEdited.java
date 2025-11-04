package io.codibase.server.event.project.codecomment;

import java.util.Date;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.User;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

public class CodeCommentEdited extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;

	public CodeCommentEdited(User user, CodeComment comment) {
		super(user, new Date(), comment);
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	protected CommentText newCommentText() {
		return new MarkdownText(getProject(), getComment().getContent());
	}

	@Override
	public String getActivity() {
		return "edited";
	}

}
