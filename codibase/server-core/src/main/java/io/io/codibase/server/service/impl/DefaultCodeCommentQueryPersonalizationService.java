package io.codibase.server.service.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.codibase.server.model.CodeCommentQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.persistence.annotation.Sessional;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.CodeCommentQueryPersonalizationService;

@Singleton
public class DefaultCodeCommentQueryPersonalizationService extends BaseEntityService<CodeCommentQueryPersonalization>
		implements CodeCommentQueryPersonalizationService {

	@Sessional
	@Override
	public CodeCommentQueryPersonalization find(Project project, User user) {
		EntityCriteria<CodeCommentQueryPersonalization> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void createOrUpdate(CodeCommentQueryPersonalization personalization) {
		if (personalization.getQueries().isEmpty()) {
			if (!personalization.isNew())
				delete(personalization);
		} else {
			dao.persist(personalization);
		}
	}
	
}
