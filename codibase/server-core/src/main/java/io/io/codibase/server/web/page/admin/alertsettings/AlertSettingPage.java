package io.codibase.server.web.page.admin.alertsettings;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.admin.AdministrationPage;

public class AlertSettingPage extends AdministrationPage {

	public AlertSettingPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var alertSetting = CodiBase.getInstance(SettingService.class).getAlertSetting();
		var oldAuditContent = VersionedXmlDoc.fromBean(alertSetting.getNotifyUsers()).toXML();

		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(alertSetting.getNotifyUsers()).toXML();
				CodiBase.getInstance(SettingService.class).saveAlertSetting(alertSetting);
				auditService.audit(null, "changed alert settings", oldAuditContent, newAuditContent);
				getSession().success(_T("Alert settings have been updated"));
				
				setResponsePage(AlertSettingPage.class);
			}
			
		};
		form.add(BeanContext.edit("editor", alertSetting));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new Label(componentId, _T("Alert Settings"));
	}

}
