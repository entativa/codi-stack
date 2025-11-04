package io.codibase.server.web.page.project;

import java.util.List;

import io.codibase.server.model.Project;
import io.codibase.server.web.page.layout.SidebarMenuItem;

public interface ProjectMenuContribution {

	List<SidebarMenuItem> getMenuItems(Project project);
	
	int getOrder();
	
}
