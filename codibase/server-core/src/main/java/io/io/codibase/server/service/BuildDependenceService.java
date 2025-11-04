package io.codibase.server.service;

import io.codibase.server.model.BuildDependence;

public interface BuildDependenceService extends EntityService<BuildDependence> {
	
	void create(BuildDependence dependence);
	
}
