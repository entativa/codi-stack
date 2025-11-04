package io.codibase.server.event.project.issue;

import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class IssuesDeleted extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Collection<Long> issueIds;
	
	public IssuesDeleted(Project project, Collection<Issue> issues) {
		super(SecurityUtils.getUser(), new Date(), project);
		issueIds = issues.stream().map(Issue::getId).collect(Collectors.toSet());
	}

	public Collection<Long> getIssueIds() {
		return issueIds;
	}

	@Override
	public String getActivity() {
		return "deleted";
	}
	
}
