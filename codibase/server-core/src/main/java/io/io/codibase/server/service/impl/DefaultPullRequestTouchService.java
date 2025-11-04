package io.codibase.server.service.impl;

import static io.codibase.server.model.PullRequestTouch.PROP_REQUEST_ID;
import static java.lang.String.format;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.event.Listen;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.entity.EntityRemoved;
import io.codibase.server.event.project.pullrequest.PullRequestChanged;
import io.codibase.server.event.project.pullrequest.PullRequestCommentCreated;
import io.codibase.server.event.project.pullrequest.PullRequestCommentEdited;
import io.codibase.server.event.project.pullrequest.PullRequestOpened;
import io.codibase.server.event.project.pullrequest.PullRequestTouched;
import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestComment;
import io.codibase.server.model.PullRequestTouch;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.ProjectService;
import io.codibase.server.service.PullRequestTouchService;

@Singleton
public class DefaultPullRequestTouchService extends BaseEntityService<PullRequestTouch>
		implements PullRequestTouchService {

	@Inject
	private ProjectService projectService;

	@Inject
	private TransactionService transactionService;

	@Inject
    private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void touch(Project project, Long requestId, boolean newRequest) {
		var projectId = project.getId();
		transactionService.runAfterCommit(() -> transactionService.runAsync(() -> {
            var innerProject = projectService.load(projectId);
            if (!newRequest) {
                var query = getSession().createQuery(format("delete from PullRequestTouch where project=:project and %s=:%s", PROP_REQUEST_ID, PROP_REQUEST_ID));
                query.setParameter("project", innerProject);
                query.setParameter(PROP_REQUEST_ID, requestId);
                query.executeUpdate();
            }

			var touch = new PullRequestTouch();
			touch.setProject(innerProject);
			touch.setRequestId(requestId);
			dao.persist(touch);

			listenerRegistry.post(new PullRequestTouched(innerProject, requestId));
		}));
	}

	@Sessional
	@Override
	public List<PullRequestTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count) {
		EntityCriteria<PullRequestTouch> criteria = EntityCriteria.of(PullRequestTouch.class);
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt(AbstractEntity.PROP_ID, afterTouchId));
		return dao.query(criteria, 0, count);
	}
	
	@Transactional
	@Listen
	public void on(PullRequestOpened event) {
		touch(event.getProject(), event.getRequest().getId(), true);
	}

	@Transactional
	@Listen
	public void on(PullRequestCommentCreated event) {
		touch(event.getProject(), event.getRequest().getId(), false);
	}

	@Transactional
	@Listen
	public void on(PullRequestCommentEdited event) {
		touch(event.getProject(), event.getRequest().getId(), false);
	}

	@Transactional
	@Listen
	public void on(PullRequestChanged event) {
		touch(event.getProject(), event.getRequest().getId(), false);
	}

	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof PullRequest) {
			PullRequest request = (PullRequest) event.getEntity();
			touch(request.getProject(), request.getId(), false);
		} else if (event.getEntity() instanceof PullRequestComment) {
			PullRequestComment comment = (PullRequestComment) event.getEntity();
			touch(comment.getRequest().getProject(), comment.getRequest().getId(), false);
		}
	}
	
}