package io.codibase.server.web.page.admin.issuesetting;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.support.administration.GlobalIssueSetting;
import io.codibase.server.web.component.issue.workflowreconcile.WorkflowChangeAlertPanel;
import io.codibase.server.web.page.admin.AdministrationPage;

public abstract class IssueSettingPage extends AdministrationPage {

	private final GlobalIssueSetting setting;
	
	public IssueSettingPage(PageParameters params) {
		super(params);
		setting = CodiBase.getInstance(SettingService.class).getIssueSetting();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new WorkflowChangeAlertPanel("workflowChangeAlert") {
			
			@Override
			protected void onCompleted(AjaxRequestTarget target) {
				setResponsePage(getPage().getClass(), getPageParameters());
			}
			
		});
	}

	public GlobalIssueSetting getSetting() {
		return setting;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new IssueSettingResourceReference()));
	}

}
