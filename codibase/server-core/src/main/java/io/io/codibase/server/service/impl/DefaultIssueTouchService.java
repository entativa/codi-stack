package io.codibase.server.service.impl;

import static io.codibase.server.model.IssueTouch.PROP_ISSUE_ID;
import static io.codibase.server.model.IssueTouch.PROP_PROJECT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.hibernate.criterion.Restrictions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.codibase.server.event.Listen;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.entity.EntityRemoved;
import io.codibase.server.event.project.issue.IssueChanged;
import io.codibase.server.event.project.issue.IssueCommentCreated;
import io.codibase.server.event.project.issue.IssueCommentEdited;
import io.codibase.server.event.project.issue.IssueOpened;
import io.codibase.server.event.project.issue.IssuesCopied;
import io.codibase.server.event.project.issue.IssuesImported;
import io.codibase.server.event.project.issue.IssuesMoved;
import io.codibase.server.event.project.issue.IssuesTouched;
import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueComment;
import io.codibase.server.model.IssueField;
import io.codibase.server.model.IssueTouch;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.IssueTouchService;
import io.codibase.server.service.ProjectService;

@Singleton
public class DefaultIssueTouchService extends BaseEntityService<IssueTouch>
		implements IssueTouchService {
	
	private static final int BATCH_SIZE = 500;

	@Inject
	private ProjectService projectService;

	@Inject
	private TransactionService transactionService;

	@Inject
	private ListenerRegistry listenerRegistry;

	@Transactional
	@Override
	public void touch(Project project, Collection<Long> issueIds, boolean newIssues) {
		var projectId = project.getId();
		transactionService.runAfterCommit(() -> transactionService.runAsync(() -> {
			var innerProject = projectService.load(projectId);
			
			if (!newIssues) {
				for (var partition: Lists.partition(new ArrayList<>(issueIds), BATCH_SIZE)) {
					CriteriaBuilder builder = getSession().getCriteriaBuilder();
					CriteriaDelete<IssueTouch> criteriaDelete = builder.createCriteriaDelete(IssueTouch.class);
					Root<IssueTouch> root = criteriaDelete.from(IssueTouch.class);
					criteriaDelete.where(
							builder.equal(root.get(PROP_PROJECT), innerProject),
							root.get(PROP_ISSUE_ID).in(partition));
					getSession().createQuery(criteriaDelete).executeUpdate();
				}
			}
			
			for (var issueId: issueIds) {
				var touch = new IssueTouch();
				touch.setProject(innerProject);
				touch.setIssueId(issueId);
				dao.persist(touch);
			}
			listenerRegistry.post(new IssuesTouched(innerProject, issueIds));
		}));
	}
	
	@Sessional
	@Override
	public List<IssueTouch> queryTouchesAfter(Long projectId, Long afterTouchId, int count) {
		EntityCriteria<IssueTouch> criteria = EntityCriteria.of(IssueTouch.class);
		criteria.add(Restrictions.eq("project.id", projectId));
		criteria.add(Restrictions.gt(AbstractEntity.PROP_ID, afterTouchId));
		return dao.query(criteria, 0, count);
	}
	
	@Transactional
	@Listen
	public void on(IssueOpened event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), true);
	}

	@Transactional
	@Listen
	public void on(IssueCommentCreated event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssueCommentEdited event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssueChanged event) {
		touch(event.getProject(), Sets.newHashSet(event.getIssue().getId()), false);
	}

	@Transactional
	@Listen
	public void on(IssuesImported event) {
		touch(event.getProject(), event.getIssueIds(), true);
	}

	@Transactional
	@Listen
	public void on(IssuesMoved event) {
		touch(event.getProject(), event.getIssueIds(), true);
	}

	@Transactional
	@Listen
	public void on(IssuesCopied event) {
		touch(event.getProject(), new HashSet<>(event.getIssueIdMapping().values()), true);
	}
	
	@Transactional
	@Listen
	public void on(EntityRemoved event) {
		if (event.getEntity() instanceof Issue) {
			Issue issue = (Issue) event.getEntity();
			touch(issue.getProject(), Sets.newHashSet(issue.getId()), false);
		} else if (event.getEntity() instanceof IssueComment) {
			IssueComment comment = (IssueComment) event.getEntity();
			touch(comment.getIssue().getProject(), Sets.newHashSet(comment.getIssue().getId()), false);
		} else if (event.getEntity() instanceof IssueField) {
			IssueField field = (IssueField) event.getEntity();
			touch(field.getIssue().getProject(), Sets.newHashSet(field.getIssue().getId()), false);
		}
	}
	
}