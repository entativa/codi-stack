package io.codibase.server.web.page.admin.labelmanagement;

import static io.codibase.server.web.translation.Translation._T;

import java.util.Comparator;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.LabelSpecService;
import io.codibase.server.model.LabelSpec;
import io.codibase.server.web.editable.BeanContext;
import io.codibase.server.web.page.admin.AdministrationPage;

public class LabelManagementPage extends AdministrationPage {

	public LabelManagementPage(PageParameters params) {
		super(params);
	}

	private LabelSpecService getLabelService() {
		return CodiBase.getInstance(LabelSpecService.class);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		LabelManagementBean bean = new LabelManagementBean();
		
		var labels = getLabelService().query();
		labels.sort(Comparator.comparing(LabelSpec::getName));
		bean.getLabels().addAll(labels);
		var oldAuditContent = VersionedXmlDoc.fromBean(bean.getLabels()).toXML();
		
		Form<?> form = new Form<Void>("form") {

			@Override
			protected void onSubmit() {
				super.onSubmit();
				var newAuditContent = VersionedXmlDoc.fromBean(bean.getLabels()).toXML();
				getLabelService().sync(bean.getLabels());
				auditService.audit(null, "changed labels", oldAuditContent, newAuditContent);
				setResponsePage(LabelManagementPage.class);
				Session.get().success(_T("Labels have been updated"));
			}
			
		};
		form.add(new FencedFeedbackPanel("feedback", form));
		form.add(BeanContext.edit("editor", bean));
		
		add(form);
	}

	@Override
	protected Component newTopbarTitle(String componentId) {
		return new org.apache.wicket.markup.html.basic.Label(componentId, _T("Labels"));
	}

}
