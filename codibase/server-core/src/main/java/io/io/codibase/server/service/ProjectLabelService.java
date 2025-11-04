package io.codibase.server.service;

import io.codibase.server.model.Project;
import io.codibase.server.model.ProjectLabel;

import java.util.Collection;

public interface ProjectLabelService extends EntityLabelService<ProjectLabel> {
	
	void create(ProjectLabel projectLabel);

	void populateLabels(Collection<Project> projects);
	
}
