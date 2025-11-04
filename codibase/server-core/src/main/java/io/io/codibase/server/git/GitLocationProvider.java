package io.codibase.server.git;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import io.codibase.server.service.SettingService;
import io.codibase.server.git.location.GitLocation;

@Singleton
public class GitLocationProvider implements Provider<GitLocation> {

	private final SettingService settingService;
	
	@Inject
	public GitLocationProvider(SettingService settingService) {
		this.settingService = settingService;
	}
	
	@Override
	public GitLocation get() {
		return settingService.getSystemSetting().getGitLocation();
	}

}
