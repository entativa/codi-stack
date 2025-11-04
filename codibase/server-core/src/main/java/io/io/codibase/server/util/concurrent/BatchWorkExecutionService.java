package io.codibase.server.util.concurrent;

public interface BatchWorkExecutionService {

	void submit(BatchWorker worker, Prioritized work);
	
}
