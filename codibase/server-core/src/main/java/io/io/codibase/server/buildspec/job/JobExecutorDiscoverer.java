package io.codibase.server.buildspec.job;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;

public interface JobExecutorDiscoverer {
	
	@Nullable
	JobExecutor discover();
	
	int getOrder();
	
}
