package io.codibase.server.event.project.issue;

import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.Issue;
import io.codibase.server.security.SecurityUtils;

import java.util.Date;

public class IssueDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long issueId;
	
	public IssueDeleted(Issue issue) {
		super(SecurityUtils.getUser(), new Date(), issue.getProject());
		issueId = issue.getId();
	}

	public Long getIssueId() {
		return issueId;
	}

	@Override
	public String getActivity() {
		return "deleted";
	}
	
}
