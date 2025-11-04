package io.codibase.server.event.agent;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentService;
import io.codibase.server.event.Event;
import io.codibase.server.model.Agent;

public abstract class AgentEvent extends Event {
	
	private final Long agentId;
	
	public AgentEvent(Agent agent) {
		agentId = agent.getId();
	}

	public Agent getAgent() {
		return CodiBase.getInstance(AgentService.class).load(agentId);
	}
	
}
