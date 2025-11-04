package io.codibase.server.model.support.pullrequest.changedata;

import io.codibase.server.CodiBase;
import io.codibase.server.service.CodeCommentService;
import io.codibase.server.entityreference.ReferencedFromAware;
import io.codibase.server.model.CodeComment;
import io.codibase.server.notification.ActivityDetail;

public class PullRequestReferencedFromCodeCommentData 
		extends PullRequestChangeData implements ReferencedFromAware<CodeComment> {

	private static final long serialVersionUID = 1L;

	private final Long commentId;
	
	public PullRequestReferencedFromCodeCommentData(CodeComment comment) {
		this.commentId = comment.getId();
	}
	
	public Long getCommentId() {
		return commentId;
	}

	@Override
	public String getActivity() {
		return "referenced from code comment";
	}

	@Override
	public boolean isMinor() {
		return true;
	}

	@Override
	public CodeComment getReferencedFrom() {
		return CodiBase.getInstance(CodeCommentService.class).get(commentId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
