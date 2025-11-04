package io.codibase.server.event.agent;

import io.codibase.server.model.Agent;

public class AgentConnected extends AgentEvent {
	
	public AgentConnected(Agent agent) {
		super(agent);
	}

}
