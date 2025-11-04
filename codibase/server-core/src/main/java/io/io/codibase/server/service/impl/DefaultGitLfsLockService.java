package io.codibase.server.service.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import com.google.common.base.Preconditions;

import io.codibase.server.model.GitLfsLock;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.persistence.dao.EntityCriteria;
import io.codibase.server.service.GitLfsLockService;

@Singleton
public class DefaultGitLfsLockService extends BaseEntityService<GitLfsLock> implements GitLfsLockService {

	@Override
	public GitLfsLock find(String path) {
		EntityCriteria<GitLfsLock> criteria = newCriteria();
		criteria.add(Restrictions.ilike(GitLfsLock.PROP_PATH, path));
		criteria.setCacheable(true);
		return find(criteria);
	}

	@Transactional
	@Override
	public void create(GitLfsLock lock) {
		Preconditions.checkState(lock.isNew());
		dao.persist(lock);
	}

}
