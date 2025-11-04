package io.codibase.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestChangeService;
import io.codibase.server.model.PullRequestChange;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestChangeActivity implements PullRequestActivity {

	private final Long changeId;
	
	public PullRequestChangeActivity(PullRequestChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Component render(String panelId) {
		return new PullRequestChangePanel(panelId);
	}

	public PullRequestChange getChange() {
		return CodiBase.getInstance(PullRequestChangeService.class).load(changeId);
	}

	@Override
	public Date getDate() {
		return getChange().getDate();
	}

}
