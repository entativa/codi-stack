package io.codibase.server.mail;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.CodiBase;
import io.codibase.server.service.AlertService;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.oauth.OAuthTokenService;
import io.codibase.server.util.oauth.RefreshTokenAccessor;
import org.unbescape.html.HtmlEscape;

public class OAuthAccessToken implements MailCredential {

	private static final long serialVersionUID = 1L;

	private final String tokenEndpoint;
	
	private final String clientId;
	
	private final String clientSecret;
	
	private final RefreshTokenAccessor refreshTokenAccessor;
	
	public OAuthAccessToken(String tokenEndpoint, String clientId, String clientSecret,
							RefreshTokenAccessor refreshTokenAccessor) {
		this.tokenEndpoint = tokenEndpoint;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.refreshTokenAccessor = refreshTokenAccessor;
	}

	@Override
	public String getValue() {
		try {
			return CodiBase.getInstance(OAuthTokenService.class).getAccessToken(tokenEndpoint, clientId, clientSecret, refreshTokenAccessor);
		} catch (ExplicitException e) {
			if (SecurityUtils.isAnonymous() || SecurityUtils.isSystem())
				CodiBase.getInstance(AlertService.class).alert("Failed to get access token of mail server",
						HtmlEscape.escapeHtml5(e.getMessage()), true);
			throw e;
		}
	}

}
