package io.codibase.server.event.project;

import io.codibase.server.event.Event;
import io.codibase.server.model.Project;

import java.io.Serializable;

public class ProjectDeleted extends Event implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final Long projectId;
	
	public ProjectDeleted(Project project) {
		projectId = project.getId();
	}

	public Long getProjectId() {
		return projectId;
	}
	
}
