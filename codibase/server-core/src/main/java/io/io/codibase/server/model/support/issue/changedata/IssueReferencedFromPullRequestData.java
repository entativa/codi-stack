package io.codibase.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestService;
import io.codibase.server.entityreference.ReferencedFromAware;
import io.codibase.server.model.Group;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.notification.ActivityDetail;

public class IssueReferencedFromPullRequestData extends IssueChangeData implements ReferencedFromAware<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Long requestId;
	
	public IssueReferencedFromPullRequestData(PullRequest request) {
		this.requestId = request.getId();
	}
	
	public Long getRequestId() {
		return requestId;
	}

	@Override
	public String getActivity() {
		return "referenced from pull request";
	}

	@Override
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}

	@Override
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}

	@Override
	public boolean affectsListing() {
		return false;
	}

	@Override
	public PullRequest getReferencedFrom() {
		return CodiBase.getInstance(PullRequestService.class).get(requestId);
	}

	@Override
	public boolean isMinor() {
		return true;
	}
	
	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}
	
}
