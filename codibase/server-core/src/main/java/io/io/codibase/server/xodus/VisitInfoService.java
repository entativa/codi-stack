package io.codibase.server.xodus;

import java.io.File;
import java.util.Date;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.Issue;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;

public interface VisitInfoService {
	
	void visitPullRequest(User user, PullRequest request);
	
	void visitPullRequestCodeComments(User user, PullRequest request);
	
	void visitIssue(User user, Issue issue);
	
	void visitCodeComment(User user, CodeComment comment);
	
	@Nullable
	Date getIssueVisitDate(User user, Issue issue);
	
	@Nullable
	Date getPullRequestVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getPullRequestCodeCommentsVisitDate(User user, PullRequest request);
	
	@Nullable
	Date getCodeCommentVisitDate(User user, CodeComment comment);

    void syncVisitInfo(Long projectId, String syncWithServer);

	void export(Long projectId, File targetDir);
	
}
