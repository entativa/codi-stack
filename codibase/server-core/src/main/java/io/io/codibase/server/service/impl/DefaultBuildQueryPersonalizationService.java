package io.codibase.server.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.BuildQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.model.support.NamedQuery;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.BuildQueryPersonalizationService;

@Singleton
public class DefaultBuildQueryPersonalizationService extends BaseEntityService<BuildQueryPersonalization>
		implements BuildQueryPersonalizationService {

	@Sessional
	@Override
	public BuildQueryPersonalization find(Project project, User user) {
		EntityCriteria<BuildQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void createOrUpdate(BuildQueryPersonalization personalization) {
		Collection<String> retainNames = new HashSet<>();
		retainNames.addAll(personalization.getQueries().stream()
				.map(it->NamedQuery.PERSONAL_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		retainNames.addAll(personalization.getProject().getNamedBuildQueries().stream()
				.map(it->NamedQuery.COMMON_NAME_PREFIX+it.getName()).collect(Collectors.toSet()));
		personalization.getQuerySubscriptionSupport().getQuerySubscriptions().retainAll(retainNames);
		
		if (personalization.getQuerySubscriptionSupport().getQuerySubscriptions().isEmpty() && personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}

}
