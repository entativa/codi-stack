package io.codibase.server.event.project.issue;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueService;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.Group;
import io.codibase.server.model.Issue;
import io.codibase.server.model.User;
import io.codibase.server.web.UrlService;

public abstract class IssueEvent extends ProjectEvent {

	private static final long serialVersionUID = 1L;
	
	private final Long issueId;
	
	private final boolean sendNotifications;

	public IssueEvent(User user, Date date, Issue issue) {
		this(user, date, issue, !user.isServiceAccount());
	}
	
	public IssueEvent(User user, Date date, Issue issue, boolean sendNotifications) {
		super(user, date, issue.getProject());
		issueId = issue.getId();
		this.sendNotifications = sendNotifications;
	}
	
	public Issue getIssue() {
		return CodiBase.getInstance(IssueService.class).load(issueId);
	}
	
	public abstract boolean affectsListing();
	
	public Map<String, Collection<User>> getNewUsers() {
		return new HashMap<>();
	}
	
	public Map<String, Group> getNewGroups() {
		return new HashMap<>();
	}
	
	@Override
	public String getLockName() {
		return Issue.getSerialLockName(issueId);
	}
	
	@Override
	public String getUrl() {
		return CodiBase.getInstance(UrlService.class).urlFor(getIssue(), true);
	}

	public boolean isSendNotifications() {
		return sendNotifications;
	}
}
