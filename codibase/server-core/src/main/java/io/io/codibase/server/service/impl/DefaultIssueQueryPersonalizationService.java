package io.codibase.server.service.impl;

import static io.codibase.server.model.AbstractEntity.PROP_ID;
import static io.codibase.server.model.IssueQueryPersonalization.PROP_PROJECT;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.criteria.CriteriaBuilder;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.IssueQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.model.support.NamedQuery;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.IssueQueryPersonalizationService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.util.ProjectScope;

@Singleton
public class DefaultIssueQueryPersonalizationService extends BaseEntityService<IssueQueryPersonalization>
		implements IssueQueryPersonalizationService {

	@Inject
	private ProjectService projectService;

	@Sessional
	@Override
	public IssueQueryPersonalization find(Project project, User user) {
		EntityCriteria<IssueQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}
	
	@Sessional
	@Override
	public Collection<IssueQueryPersonalization> query(ProjectScope projectScope) {
		CriteriaBuilder builder = getSession().getCriteriaBuilder();
		var criteriaQuery = builder.createQuery(IssueQueryPersonalization.class);
		var root = criteriaQuery.from(IssueQueryPersonalization.class);
		
		var projectId = projectScope.getProject().getId();
		Collection<Long> checkIds = new HashSet<>();
		if (projectScope.isInherited()) {
			for (Project ancestor: projectScope.getProject().getAncestors())
				checkIds.add(ancestor.getId());
		}
		if (projectScope.isRecursive()) 
			checkIds.addAll(projectService.getSubtreeIds(projectId));
		else 
			checkIds.add(projectId);
		criteriaQuery.where(root.get(PROP_PROJECT).get(PROP_ID).in(checkIds));
		var query = getSession().createQuery(criteriaQuery);
		query.setFirstResult(0);
		query.setMaxResults(Integer.MAX_VALUE);
		return query.getResultList();
	}

	@Transactional
	@Override
	public void createOrUpdate(IssueQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedIssueQueries().stream()
				.map(it->NamedQuery.COMMON_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQueryWatchSupport().getQueryWatches().keySet().retainAll(retainNames);

		if (personalization.getQueryWatchSupport().getQueryWatches().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}
	
}
