package io.codibase.server.event.agent;

import io.codibase.server.model.Agent;

public class AgentDisconnected extends AgentEvent {
	
	public AgentDisconnected(Agent agent) {
		super(agent);
	}

}
