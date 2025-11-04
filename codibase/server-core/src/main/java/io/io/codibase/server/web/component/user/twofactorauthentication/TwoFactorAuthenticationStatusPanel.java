package io.codibase.server.web.component.user.twofactorauthentication;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.web.page.user.UserPage;

public abstract class TwoFactorAuthenticationStatusPanel extends Panel {
	public TwoFactorAuthenticationStatusPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Fragment fragment;
		if (getUser().getTwoFactorAuthentication() != null) {
			fragment = new Fragment("content", "configuredFrag", this);
			fragment.add(new Link<Void>("requestToSetupAgain") {

				@Override
				public void onClick() {
					getUser().setTwoFactorAuthentication(null);
					CodiBase.getInstance(UserService.class).update(getUser(), null);
					if (getPage() instanceof UserPage) {
						CodiBase.getInstance(AuditService.class).audit(null, "reset two factor authentication of account \"" + getUser().getName() + "\"", null, null);
					}
					setResponsePage(getPage().getPageClass(), getPage().getPageParameters());
				}
			});
		} else {
			fragment = new Fragment("content", "notConfiguredFrag", this);
		}
		add(fragment);
	}

	protected abstract User getUser();
	
}
