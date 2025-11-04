package io.codibase.server.web.page.my;

import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.User;
import io.codibase.server.web.page.layout.LayoutPage;
import io.codibase.server.web.page.security.LoginPage;
import io.codibase.server.web.util.UserAware;

public abstract class MyPage extends LayoutPage implements UserAware {
	
	public MyPage(PageParameters params) {
		super(params);
		if (getUser() == null) 
			throw new RestartResponseAtInterceptPageException(LoginPage.class);
	}

	@Override
	public User getUser() {
		return getLoginUser();
	}

	@Override
	protected String getPageTitle() {
		return "My - " + CodiBase.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
}
