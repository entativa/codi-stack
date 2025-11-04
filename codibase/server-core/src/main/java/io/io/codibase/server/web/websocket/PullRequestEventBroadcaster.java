package io.codibase.server.web.websocket;

import io.codibase.server.event.Listen;
import io.codibase.server.event.project.pullrequest.PullRequestEvent;
import io.codibase.server.model.PullRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PullRequestEventBroadcaster {
	
	private final WebSocketService webSocketService;
	
	@Inject
	public PullRequestEventBroadcaster(WebSocketService webSocketService) {
		this.webSocketService = webSocketService;
	}

	@Listen
	public void on(PullRequestEvent event) {
		webSocketService.notifyObservableChange(PullRequest.getChangeObservable(event.getRequest().getId()), event.getSourcePage());
	}

}