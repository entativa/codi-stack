package io.codibase.server.taskschedule;

public interface TaskScheduler {
	
	void start();
	
	String schedule(SchedulableTask task);
	
	void unschedule(String taskId);

	void stop();
	
}
