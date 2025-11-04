package io.codibase.server.pack;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Pack;
import io.codibase.server.model.Project;
import org.apache.wicket.Component;

import java.io.Serializable;

@ExtensionPoint
public interface PackSupport extends Serializable {
	
	int getOrder();
	
	String getPackType();
	
	String getPackIcon();
	
	String getReference(Pack pack, boolean withProject);
	
	Component renderContent(String componentId, Pack pack);
	
	Component renderHelp(String componentId, Project project);
	
}
