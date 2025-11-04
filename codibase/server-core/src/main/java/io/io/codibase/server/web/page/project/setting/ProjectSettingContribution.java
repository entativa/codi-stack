package io.codibase.server.web.page.project.setting;

import java.util.List;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ProjectSettingContribution {

	List<Class<? extends ContributedProjectSetting>> getSettingClasses();
	
}
