package io.codibase.server.web.page.project.issues.detail;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.web.component.issue.pullrequests.IssuePullRequestsPanel;

public class IssuePullRequestsPage extends IssueDetailPage {

	public IssuePullRequestsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new IssuePullRequestsPanel("pullRequests", issueModel));
	}
	
}
