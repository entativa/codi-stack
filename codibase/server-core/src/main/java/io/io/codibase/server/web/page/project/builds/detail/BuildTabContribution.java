package io.codibase.server.web.page.project.builds.detail;

import java.util.List;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Build;

@ExtensionPoint
public interface BuildTabContribution {
	
	List<BuildTab> getTabs(Build build);
	
	int getOrder();
	
}
