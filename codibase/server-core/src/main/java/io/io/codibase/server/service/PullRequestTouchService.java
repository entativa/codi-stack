package io.codibase.server.service;

import java.util.List;

import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequestTouch;

public interface PullRequestTouchService extends EntityService<PullRequestTouch> {
	
	void touch(Project project, Long requestId, boolean newRequest);
	
	List<PullRequestTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count);
	
}
