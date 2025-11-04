package io.codibase.server.event.project.codecomment;

import org.jspecify.annotations.Nullable;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentStatusChangeService;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.CodeCommentStatusChange;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;

public class CodeCommentStatusChanged extends CodeCommentEvent {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public CodeCommentStatusChanged(CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), change.getComment());
		changeId = change.getId();
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return CodiBase.getInstance(CodeCommentStatusChangeService.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null?new MarkdownText(getProject(), note):null;
	}

	@Nullable
	public String getNote() {
		return note;
	}

	@Override
	public String getActivity() {
		if (getChange().isResolved())
			return "resolved";
		else
			return "unresolved";
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getChange(), true);
	}
	
}
