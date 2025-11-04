package io.codibase.server.job.log;

public interface LogListener {
	
	void logged(Long buildId);
	
}
