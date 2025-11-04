package io.codibase.server.web.page.admin.servicedesk;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.editable.BeanEditor;
import io.codibase.server.web.page.admin.AdministrationPage;
import io.codibase.server.web.page.admin.mailservice.MailConnectorPage;

public class ServiceDeskSettingPage extends AdministrationPage {

	private String oldAuditContent;

	public ServiceDeskSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new BookmarkablePageLink<Void>("mailConnector", MailConnectorPage.class));
		
		ServiceDeskSettingHolder serviceDeskSettingHolder = new ServiceDeskSettingHolder();
		serviceDeskSettingHolder.setServiceDeskSetting(CodiBase.getInstance(SettingService.class).getServiceDeskSetting());
		oldAuditContent = VersionedXmlDoc.fromBean(serviceDeskSettingHolder.getServiceDeskSetting()).toXML();
		
		BeanEditor editor = BeanContext.edit("editor", serviceDeskSettingHolder);
		
		Button saveButton = new Button("save") {

			@Override
			public void onSubmit() {
				super.onSubmit();

				var newAuditContent = VersionedXmlDoc.fromBean(serviceDeskSettingHolder.getServiceDeskSetting()).toXML();
				CodiBase.getInstance(SettingService.class).saveServiceDeskSetting(serviceDeskSettingHolder.getServiceDeskSetting());
				auditService.audit(null, "changed service desk settings", oldAuditContent, newAuditContent);
				oldAuditContent = newAuditContent;
				getSession().success(_T("Service desk settings have been saved"));
			}
			
		};
		
		Form<?> form = new Form<Void>("form");
		
		form.add(editor);
		form.add(saveButton);
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Service Desk Settings"));
	}

}