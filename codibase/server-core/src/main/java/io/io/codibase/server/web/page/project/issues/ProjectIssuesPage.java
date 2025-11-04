package io.codibase.server.web.page.project.issues;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.codibase.server.web.page.project.ProjectPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class ProjectIssuesPage extends ProjectPage {

	public ProjectIssuesPage(PageParameters params) {
		super(params);
	}

	protected GlobalIssueSetting getIssueSetting() {
		return CodiBase.getInstance(SettingService.class).getIssueSetting();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {

			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPageClass(), getPageParameters());
			}
			
		});
	}

	@Override
	protected String getPageTitle() {
		return "Issues - " + getProject().getPath();
	}
	
}
