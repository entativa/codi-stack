package io.codibase.server.web.page.layout;

import java.util.List;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface AdministrationSettingContribution {

	List<Class<? extends ContributedAdministrationSetting>> getSettingClasses();
	
}
