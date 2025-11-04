package io.codibase.server.model.support.pullrequest.changedata;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.entityreference.ReferencedFromAware;
import io.codibase.server.model.PullRequest;
import io.codibase.server.notification.ActivityDetail;

public class PullRequestReferencedFromPullRequestData 
		extends PullRequestChangeData implements ReferencedFromAware<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public PullRequestReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public String getActivity() {
		return "referenced from other pull request";
	}

	@Override
	public PullRequest getReferencedFrom() {
		return CodiBase.getInstance(PullRequestService.class).get(requestId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
