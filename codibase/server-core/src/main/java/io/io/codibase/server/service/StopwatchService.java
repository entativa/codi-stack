package io.codibase.server.service;

import io.codibase.server.model.Issue;
import io.codibase.server.model.Stopwatch;
import io.codibase.server.model.User;

import org.jspecify.annotations.Nullable;

public interface StopwatchService extends EntityService<Stopwatch> {
	
	@Nullable
	Stopwatch find(User user, Issue issue);
	
	Stopwatch startWork(User user, Issue issue);
	
	void stopWork(Stopwatch stopwatch);
	
}
