package io.codibase.server.model.support.administration.sso;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.page.security.SsoProcessPage;

@Editable
public abstract class SsoConnector implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract String getButtonImageUrl();
	
	public final URI getCallbackUri(String providerName) {
		String serverUrl = CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
		try {
			return new URI(serverUrl + "/" + SsoProcessPage.MOUNT_PATH + "/" 
					+ SsoProcessPage.STAGE_CALLBACK + "/" + providerName);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public abstract SsoAuthenticated handleAuthResponse(String providerName);
	
	public abstract String buildAuthUrl(String providerName);

}
