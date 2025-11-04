package io.codibase.server.web.page.project.blob.search;

import java.util.List;

import io.codibase.server.web.component.floating.FloatingPanel;
import io.codibase.server.web.component.menu.MenuItem;

public interface SearchMenuContributor {
	
	List<MenuItem> getMenuItems(FloatingPanel dropdown);
	
}
