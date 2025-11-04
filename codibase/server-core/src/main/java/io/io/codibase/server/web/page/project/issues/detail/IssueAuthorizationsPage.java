package io.codibase.server.web.page.project.issues.detail;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.model.Issue;
import io.codibase.server.web.component.issue.authorizations.IssueAuthorizationsPanel;

public class IssueAuthorizationsPage extends IssueDetailPage {

	public IssueAuthorizationsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new IssueAuthorizationsPanel("authorizations") {

			@Override
			protected Issue getIssue() {
				return IssueAuthorizationsPage.this.getIssue();
			}

		});
	}

}
