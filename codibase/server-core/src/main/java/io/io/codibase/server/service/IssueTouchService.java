package io.codibase.server.service;

import io.codibase.server.model.IssueTouch;
import io.codibase.server.model.Project;

import java.util.Collection;
import java.util.List;

public interface IssueTouchService extends EntityService<IssueTouch> {
	
	void touch(Project project, Collection<Long> issueIds, boolean newIssues);
	
	List<IssueTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
