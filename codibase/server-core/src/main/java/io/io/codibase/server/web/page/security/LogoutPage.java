package io.codibase.server.web.page.security;

import io.codibase.server.web.WebSession;
import io.codibase.server.web.page.base.BasePage;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class LogoutPage extends BasePage {

	public LogoutPage(PageParameters params) {
		super(params);
		WebSession.get().logout();
		getSession().warn(_T("You've been logged out"));
		throw new RestartResponseException(getApplication().getHomePage());
	}
	
}
