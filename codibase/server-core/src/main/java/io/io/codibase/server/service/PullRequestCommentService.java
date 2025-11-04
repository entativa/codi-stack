package io.codibase.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import io.codibase.server.model.PullRequestComment;
import io.codibase.server.model.User;

public interface PullRequestCommentService extends EntityService<PullRequestComment> {

	void create(PullRequestComment comment);

	void create(PullRequestComment comment, Collection<String> notifiedEmailAddresses);

	void update(PullRequestComment comment);

	void delete(User user, PullRequestComment comment);
	
	List<PullRequestComment> query(User submitter, Date fromDate, Date toDate);

}
