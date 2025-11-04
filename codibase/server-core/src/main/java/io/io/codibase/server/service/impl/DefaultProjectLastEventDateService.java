package io.codibase.server.service.impl;

import java.util.Date;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.event.Listen;
import io.codibase.server.event.project.ProjectCreated;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.event.project.RefUpdated;
import io.codibase.server.model.Project;
import io.codibase.server.model.ProjectLastActivityDate;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.ProjectLastEventDateService;

@Singleton
public class DefaultProjectLastEventDateService extends BaseEntityService<ProjectLastActivityDate> implements ProjectLastEventDateService {

	@Transactional
	@Listen
	public void on(ProjectEvent event) {
		Project project = event.getProject();
		if (event instanceof RefUpdated) {
			project.getLastActivityDate().setValue(new Date());
		} else if (!(event instanceof ProjectCreated) 
				&& event.getUser() != null 
				&& !event.getUser().isSystem()) {
			project.getLastActivityDate().setValue(new Date());
		}
	}
	
	@Transactional
	@Override
	public void create(ProjectLastActivityDate lastEventDate) {
		Preconditions.checkState(lastEventDate.isNew());
		dao.persist(lastEventDate);
	}
	
}
