package io.codibase.server.web.component.user.accesstoken;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.google.common.collect.Sets;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.AccessTokenService;
import io.codibase.server.service.AuditService;
import io.codibase.server.model.AccessToken;
import io.codibase.server.util.CryptoUtils;
import io.codibase.server.web.ajaxlistener.ConfirmClickListener;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.user.UserPage;

abstract class AccessTokenPanel extends Panel {
	
	public AccessTokenPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var token = getToken();
		add(new Label("name", token.getName()));
		add(new AjaxLink<Void>("edit") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onEdit(target);
			}
			
		});
		add(new AjaxLink<Void>("regenerate") {
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener("This will invalidate current access token and " +
						"generate a new one. Do you really want to continue?"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				var oldAuditContent = VersionedXmlDoc.fromBean(token).toXML();
				var token = getToken();
				token.setValue(CryptoUtils.generateSecret());
				var newAuditContent = VersionedXmlDoc.fromBean(token).toXML();
				CodiBase.getInstance(AccessTokenService.class).createOrUpdate(token);
				if (getPage() instanceof UserPage) {
					CodiBase.getInstance(AuditService.class).audit(null, "regenerated access token \"" + token.getName() + "\" in account \"" + token.getOwner().getName() + "\"", oldAuditContent, newAuditContent);
				}
				target.add(AccessTokenPanel.this);
				Session.get().success(_T("Access token regenerated successfully"));
			}
		});
		add(new AjaxLink<Void>("delete") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmClickListener(_T("Do you really want to delete this access token?")));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				onDelete(target);
			}
			
		});
		
		add(new WebMarkupContainer("expired").setVisible(getToken().isExpired()));
		
		setOutputMarkupId(true);
	}

	@Override
	protected void onBeforeRender() {
		addOrReplace(BeanContext.view("viewer", AccessTokenEditBean.of(getToken()), Sets.newHashSet("name"), true));
		super.onBeforeRender();
	}

	protected abstract AccessToken getToken();
	
	protected abstract void onDelete(AjaxRequestTarget target);

	protected abstract void onEdit(AjaxRequestTarget target);
	
}
