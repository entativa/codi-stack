package io.codibase.server.service.impl;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Preconditions;
import io.codibase.server.service.PullRequestAssignmentService;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.pullrequest.PullRequestAssigned;
import io.codibase.server.event.project.pullrequest.PullRequestUnassigned;
import io.codibase.server.model.PullRequestAssignment;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.security.SecurityUtils;

@Singleton
public class DefaultPullRequestAssignmentService extends BaseEntityService<PullRequestAssignment>
		implements PullRequestAssignmentService {

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void create(PullRequestAssignment assignment) {
		Preconditions.checkState(assignment.isNew());
		dao.persist(assignment);

		listenerRegistry.post(new PullRequestAssigned(
				SecurityUtils.getUser(), new Date(), 
				assignment.getRequest(), assignment.getUser()));
	}

	@Transactional
	@Override
	public void delete(PullRequestAssignment assignment) {
		super.delete(assignment);
		
		listenerRegistry.post(new PullRequestUnassigned(
				SecurityUtils.getUser(), new Date(), 
				assignment.getRequest(), assignment.getUser()));
	}
		
}
