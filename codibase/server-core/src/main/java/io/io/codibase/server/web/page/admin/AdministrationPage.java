package io.codibase.server.web.page.admin;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.page.layout.LayoutPage;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AdministrationPage extends LayoutPage {

	public AdministrationPage(PageParameters params) {
		super(params);
	}

	@Override
	protected boolean isPermitted() {
		return SecurityUtils.isAdministrator();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new AdministrationCssResourceReference()));
	}

	@Override
	protected String getPageTitle() {
		return "Administration - " + CodiBase.getInstance(SettingService.class).getBrandingSetting().getName();
	}
	
}
