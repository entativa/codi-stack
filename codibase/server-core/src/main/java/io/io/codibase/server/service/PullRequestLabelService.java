package io.codibase.server.service;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestLabel;

import java.util.Collection;

public interface PullRequestLabelService extends EntityLabelService<PullRequestLabel> {

	void create(PullRequestLabel pullRequestLabel);
	
	void populateLabels(Collection<PullRequest> pullRequests);
	
}
