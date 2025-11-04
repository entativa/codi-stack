package io.codibase.server.model.support.issue.changedata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.entityreference.ReferencedFromAware;
import io.codibase.server.model.Group;
import io.codibase.server.model.Issue;
import io.codibase.server.model.User;
import io.codibase.server.notification.ActivityDetail;

public class IssueReferencedFromIssueData extends IssueChangeData implements ReferencedFromAware<Issue> {

	private static final long serialVersionUID = 1L;

	private final Long issueId;
	
	public IssueReferencedFromIssueData(Issue issue) {
		this.issueId = issue.getId();
	}
	
	public Long getIssueId() {
		return issueId;
	}

	@Override
	public String getActivity() {
		return "referenced from other issue";
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
	public boolean isMinor() {
		return true;
	}

	@Override
	public Issue getReferencedFrom() {
		return CodiBase.getInstance(IssueService.class).get(issueId);
	}

	@Override
	public ActivityDetail getActivityDetail() {
		return ActivityDetail.referencedFrom(getReferencedFrom());
	}

}
