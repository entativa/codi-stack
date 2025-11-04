package io.codibase.server.web.websocket;

import io.codibase.server.event.Listen;
import io.codibase.server.event.project.build.BuildEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BuildEventBroadcaster {
	
	private final WebSocketService webSocketService;
	
	@Inject
	public BuildEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(BuildEvent event) {
		webSocketService.notifyObservablesChange(event.getBuild().getChangeObservables(), event.getSourcePage());
	}

}