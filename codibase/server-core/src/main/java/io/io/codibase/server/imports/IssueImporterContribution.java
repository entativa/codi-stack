package io.codibase.server.imports;

import java.util.Collection;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface IssueImporterContribution {

	Collection<IssueImporter> getImporters();
	
	int getOrder();
	
}
