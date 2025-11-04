package io.codibase.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueWorkService;
import io.codibase.server.model.IssueWork;

public class IssueWorkActivity implements IssueActivity {

	private final Long workId;
	
	public IssueWorkActivity(IssueWork work) {
		workId = work.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new IssueWorkPanel(panelId);
	}
	
	public Long getWorkId() {
		return workId;
	}
	
	public IssueWork getWork() {
		return CodiBase.getInstance(IssueWorkService.class).load(workId);
	}
	
	@Override
	public Date getDate() {
		return getWork().getDate();
	}
	
}
