package io.codibase.server.web.component.issue.activities.activity;

import java.util.Date;

import org.apache.wicket.markup.html.panel.Panel;

import io.codibase.server.CodiBase;
import io.codibase.server.service.IssueCommentService;
import io.codibase.server.model.IssueComment;

public class IssueCommentActivity implements IssueActivity {

	private final Long commentId;
		
	public IssueCommentActivity(IssueComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Panel render(String panelId) {
		return new IssueCommentPanel(panelId);
	}
	
	public Long getCommentId() {
		return commentId;
	}
	
	public IssueComment getComment() {
		return CodiBase.getInstance(IssueCommentService.class).load(commentId);
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}
	
}
