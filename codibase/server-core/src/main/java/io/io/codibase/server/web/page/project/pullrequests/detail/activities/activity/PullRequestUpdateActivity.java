package io.codibase.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestUpdateService;
import io.codibase.server.model.PullRequestUpdate;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestUpdateActivity implements PullRequestActivity {

	private final Long updateId;
	
	public PullRequestUpdateActivity(PullRequestUpdate update) {
		updateId = update.getId();
	}
	
	@Override
	public Component render(String componentId) {
		return new PullRequestUpdatePanel(componentId);
	}

	public PullRequestUpdate getUpdate() {
		return CodiBase.getInstance(PullRequestUpdateService.class).load(updateId);
	}

	@Override
	public Date getDate() {
		return getUpdate().getDate();
	}

}
