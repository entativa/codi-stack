package io.codibase.server.event.project.pullrequest;

import org.jspecify.annotations.Nullable;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentStatusChangeService;
import io.codibase.server.model.CodeCommentStatusChange;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.commenttext.CommentText;
import io.codibase.server.util.commenttext.MarkdownText;
import io.codibase.server.web.UrlService;

public class PullRequestCodeCommentStatusChanged extends PullRequestCodeCommentEvent {

	private static final long serialVersionUID = 1L;

	private final Long changeId;
	
	private final String note;
	
	public PullRequestCodeCommentStatusChanged(PullRequest request, 
			CodeCommentStatusChange change, @Nullable String note) {
		super(change.getUser(), change.getDate(), request, change.getComment());
		changeId = change.getId();
		this.note = note;
	}

	public CodeCommentStatusChange getChange() {
		return CodiBase.getInstance(CodeCommentStatusChangeService.class).load(changeId);
	}

	@Override
	protected CommentText newCommentText() {
		return note!=null? new MarkdownText(getProject(), note): null;
	}

	@Override
	public String getActivity() {
		if (getChange().isResolved())
			return "resolved code comment"; 
		else
			return "unresolved code comment";
	}

	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getChange(), true);
	}

}
