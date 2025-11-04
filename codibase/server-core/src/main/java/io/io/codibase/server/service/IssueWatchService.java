package io.codibase.server.service;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueWatch;
import io.codibase.server.model.User;
import io.codibase.server.util.watch.WatchStatus;

import org.jspecify.annotations.Nullable;
import java.util.Collection;

public interface IssueWatchService extends EntityService<IssueWatch> {
	
	@Nullable
	IssueWatch find(Issue issue, User user);

	void watch(Issue issue, User user, boolean watching);

    void createOrUpdate(IssueWatch watch);
	
	void setWatchStatus(User user, Collection<Issue> issues, WatchStatus watchStatus);
	
}
