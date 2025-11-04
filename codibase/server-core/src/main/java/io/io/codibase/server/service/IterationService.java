package io.codibase.server.service;

import io.codibase.server.model.Iteration;
import io.codibase.server.model.Project;

import org.jspecify.annotations.Nullable;

public interface IterationService extends EntityService<Iteration> {
	
	@Nullable
    Iteration findInHierarchy(Project project, String name);
	
	void delete(Iteration iteration);

	Iteration findInHierarchy(String iterationFQN);

    void createOrUpdate(Iteration iteration);
	
}