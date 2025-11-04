package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.AgentLastUsedDate;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.AgentLastUsedDateService;

@Singleton
public class DefaultAgentLastUsedDateService extends BaseEntityService<AgentLastUsedDate> implements AgentLastUsedDateService {

	@Transactional
	@Override
	public void create(AgentLastUsedDate lastUsedDate) {
		Preconditions.checkState(lastUsedDate.isNew());
		dao.persist(lastUsedDate);
	}
	
}
