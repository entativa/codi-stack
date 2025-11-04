package io.codibase.server.web.component.user;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.UserService;
import io.codibase.server.model.User;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.web.WebSession;
import io.codibase.server.web.page.admin.usermanagement.UserListPage;
import io.codibase.server.web.util.ConfirmClickModifier;

public abstract class UserDeleteLink extends Link<Void> {

	public UserDeleteLink(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new ConfirmClickModifier(_T("Do you really want to remove this account?")));		
	}

	@Override
	public void onClick() {
		var userService = CodiBase.getInstance(UserService.class);
		var auditService = CodiBase.getInstance(AuditService.class);
		var oldAuditContent = VersionedXmlDoc.fromBean(getUser()).toXML();
		if (getUser().equals(SecurityUtils.getAuthUser())) {
			userService.delete(getUser());
			auditService.audit(null, "deleted account \"" + getUser().getName() + "\"", oldAuditContent, null);
			WebSession.get().success("Account removed");
			WebSession.get().logout();
			throw new RestartResponseException(getApplication().getHomePage());
		} else {
			userService.delete(getUser());
			auditService.audit(null, "deleted account \"" + getUser().getName() + "\"", oldAuditContent, null);
			WebSession.get().success("Account removed");
				String redirectUrlAfterDelete = WebSession.get().getRedirectUrlAfterDelete(User.class);
			if (redirectUrlAfterDelete != null)
				throw new RedirectToUrlException(redirectUrlAfterDelete);
			else
				setResponsePage(UserListPage.class);
		}
	}

	protected abstract User getUser();
	
}
