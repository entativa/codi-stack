package io.codibase.server.timetracking;

import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.web.page.layout.SidebarMenuItem;

import java.util.Collection;

public interface TimeTrackingService {
	
	void requestToSyncTimes(Collection<Long> issueIds);
	
	int aggregateSourceLinkEstimatedTime(Issue issue, String linkName);
	
	int aggregateTargetLinkEstimatedTime(Issue issue, String linkName);

	int aggregateSourceLinkSpentTime(Issue issue, String linkName);

	int aggregateTargetLinkSpentTime(Issue issue, String linkName);

	SidebarMenuItem.Page newTimesheetsMenuItem(Project project);
	
}
