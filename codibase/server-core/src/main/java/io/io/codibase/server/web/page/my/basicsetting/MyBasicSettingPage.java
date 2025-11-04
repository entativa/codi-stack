package io.codibase.server.web.page.my.basicsetting;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.model.User;
import io.codibase.server.web.component.user.basicsetting.BasicSettingPanel;
import io.codibase.server.web.page.my.MyPage;

public class MyBasicSettingPage extends MyPage {

	public MyBasicSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new BasicSettingPanel("content", new AbstractReadOnlyModel<>() {

			@Override
			public User getObject() {
				return getUser();
			}

		}));
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("My Basic Settings"));
	}

}
