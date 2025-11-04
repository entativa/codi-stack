package io.codibase.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueChangeService;
import io.codibase.server.model.IssueChange;

public class IssueChangeActivity implements IssueActivity {

	private final Long changeId;
	
	public IssueChangeActivity(IssueChange change) {
		changeId = change.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new IssueChangePanel(panelId);
	}

	public IssueChange getChange() {
		return CodiBase.getInstance(IssueChangeService.class).load(changeId);
	}
	
	@Override
	public Date getDate() {
		return getChange().getDate();
	}

}
