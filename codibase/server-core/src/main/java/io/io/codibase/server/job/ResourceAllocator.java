package io.codibase.server.job;

import io.codibase.server.cluster.ClusterTask;
import io.codibase.server.search.entity.agent.AgentQuery;

public interface ResourceAllocator {

	boolean runServerJob(String executorName, int totalConcurrency, int requiredConcurrency, 
					  ClusterTask<Boolean> runnable);

	boolean runAgentJob(AgentQuery agentQuery, String executorName, int totalConcurrency,
								int requiredConcurrency, AgentRunnable runnable);
	
	void agentDisconnecting(Long agentId);

}
