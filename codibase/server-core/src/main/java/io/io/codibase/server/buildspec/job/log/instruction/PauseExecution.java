package io.codibase.server.buildspec.job.log.instruction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.build.BuildUpdated;
import io.codibase.server.model.Build;
import io.codibase.server.persistence.annotation.Transactional;

@Singleton
public class PauseExecution extends LogInstruction {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public PauseExecution(ListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}
	
	@Override
	public String getName() {
		return "PauseExecution";
	}

	@Transactional
	@Override
	public void execute(Build build, Map<String, List<String>> params, TaskLogger taskLogger) {
		build.setPaused(true);
		listenerRegistry.post(new BuildUpdated(build));
	}

}
