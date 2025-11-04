package io.codibase.server.web.page.project.pullrequests.detail.activities.activity;

import java.util.Date;

import org.apache.wicket.Component;

import io.codibase.server.CodiBase;
import io.codibase.server.service.PullRequestCommentService;
import io.codibase.server.model.PullRequestComment;
import io.codibase.server.web.page.project.pullrequests.detail.activities.PullRequestActivity;

public class PullRequestCommentActivity implements PullRequestActivity {

	private final Long commentId;

	public PullRequestCommentActivity(PullRequestComment comment) {
		commentId = comment.getId();
	}
	
	@Override
	public Component render(String componentId) {
		return new PullRequestCommentPanel(componentId);
	}

	
	public PullRequestComment getComment() {
		return CodiBase.getInstance(PullRequestCommentService.class).load(commentId);
	}

	@Override
	public Date getDate() {
		return getComment().getDate();
	}

}
