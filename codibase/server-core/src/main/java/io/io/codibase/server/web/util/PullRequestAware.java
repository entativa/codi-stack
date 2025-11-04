package io.codibase.server.web.util;

import io.codibase.server.model.PullRequest;

public interface PullRequestAware {
	
	PullRequest getPullRequest();
	
}
