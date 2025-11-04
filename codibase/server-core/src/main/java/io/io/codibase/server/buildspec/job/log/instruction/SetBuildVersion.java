package io.codibase.server.buildspec.job.log.instruction;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.event.ListenerRegistry;
import io.codibase.server.event.project.build.BuildUpdated;
import io.codibase.server.model.Build;
import io.codibase.server.persistence.annotation.Transactional;

@Singleton
public class SetBuildVersion extends LogInstruction {

	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public SetBuildVersion(ListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}
	
	@Override
	public String getName() {
		return "SetBuildVersion";
	}

	@Transactional
	@Override
	public void execute(Build build, Map<String, List<String>> params, TaskLogger taskLogger) {
		String version = params.values().iterator().next().iterator().next();
		if (StringUtils.isNotBlank(version))
			build.setVersion(version);
		else
			build.setVersion(null);
		listenerRegistry.post(new BuildUpdated(build));
	}

}
