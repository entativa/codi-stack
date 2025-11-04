package io.codibase.server.service;

import io.codibase.server.model.AgentLastUsedDate;

public interface AgentLastUsedDateService extends EntityService<AgentLastUsedDate> {

	void create(AgentLastUsedDate lastUsedDate);
	
}
