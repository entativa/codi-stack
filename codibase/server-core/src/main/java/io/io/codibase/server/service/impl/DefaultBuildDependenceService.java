package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.BuildDependence;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.BuildDependenceService;

@Singleton
public class DefaultBuildDependenceService extends BaseEntityService<BuildDependence> implements BuildDependenceService {

	@Transactional
	@Override
	public void create(BuildDependence dependence) {
		Preconditions.checkState(dependence.isNew());
		dao.persist(dependence);
	}
	
}
