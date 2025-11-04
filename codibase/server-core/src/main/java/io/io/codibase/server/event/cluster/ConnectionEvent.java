package io.codibase.server.event.cluster;

import io.codibase.server.event.Event;

public abstract class ConnectionEvent extends Event {
	
	private final String server;
	
	public ConnectionEvent(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}
	
}
