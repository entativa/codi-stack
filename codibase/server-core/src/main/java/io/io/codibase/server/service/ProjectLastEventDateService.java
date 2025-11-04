package io.codibase.server.service;

import io.codibase.server.model.ProjectLastActivityDate;

public interface ProjectLastEventDateService extends EntityService<ProjectLastActivityDate> {

	void create(ProjectLastActivityDate lastEventDate);
	
}
