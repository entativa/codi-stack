package io.codibase.server.service;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestWatch;
import io.codibase.server.model.User;
import io.codibase.server.util.watch.WatchStatus;

import java.util.Collection;

public interface PullRequestWatchService extends EntityService<PullRequestWatch> {
	
	@Nullable
	PullRequestWatch find(PullRequest request, User user);
	
	void watch(PullRequest request, User user, boolean watching);

    void createOrUpdate(PullRequestWatch watch);

	void setWatchStatus(User user, Collection<PullRequest> requests, WatchStatus watchStatus);
	
}
