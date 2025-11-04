package io.codibase.server.taskschedule;

import org.quartz.ScheduleBuilder;

public interface SchedulableTask {
	void execute();
	
	ScheduleBuilder<?> getScheduleBuilder();
}
