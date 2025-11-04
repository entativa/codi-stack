package io.codibase.server.job.log;

import org.jspecify.annotations.Nullable;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.CodiBase;
import io.codibase.server.cluster.ClusterTask;

public class LogTask implements ClusterTask<Void> {

	private static final long serialVersionUID = 1L;

	private final String jobToken;
	
	private final String message;
	
	private final String sessionId;
	
	public LogTask(String jobToken, String message, @Nullable String sessionId) {
		this.jobToken = jobToken;
		this.message = message;
		this.sessionId = sessionId;
	}
	
	@Override
	public Void call() {
		TaskLogger logger = CodiBase.getInstance(LogService.class).getJobLogger(jobToken);
		if (logger != null && !(logger instanceof ServerJobLogger))  
			logger.log(message, sessionId);
		return null;
	}
	
}