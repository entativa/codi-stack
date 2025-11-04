package io.codibase.server.web.page.my.accesstoken;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.model.User;
import io.codibase.server.web.component.user.accesstoken.AccessTokenListPanel;
import io.codibase.server.web.page.my.MyPage;

public class MyAccessTokensPage extends MyPage {

	public MyAccessTokensPage(PageParameters params) {
		super(params);
		if (getUser().isDisabled())
			throw new IllegalStateException();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new AccessTokenListPanel("accessTokens") {
			
			@Override
			protected User getUser() {
				return getLoginUser();
			}
			
		});
		
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My Access Tokens"));
	}

}
