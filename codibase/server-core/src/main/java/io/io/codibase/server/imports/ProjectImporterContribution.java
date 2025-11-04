package io.codibase.server.imports;

import java.util.Collection;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectImporterContribution {

	Collection<ProjectImporter> getImporters();
	
	int getOrder();
	
}
