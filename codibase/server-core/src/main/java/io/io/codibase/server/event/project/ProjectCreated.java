package io.codibase.server.event.project;

import java.util.Date;

import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;

public class ProjectCreated extends ProjectEvent {
	
	public ProjectCreated(Project project) {
		super(SecurityUtils.getUser(), new Date(), project);
	}

	@Override
	public String getActivity() {
		return "created";
	}

}
