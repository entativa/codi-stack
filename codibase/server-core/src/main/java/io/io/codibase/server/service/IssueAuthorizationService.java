package io.codibase.server.service;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueAuthorization;
import io.codibase.server.model.User;

public interface IssueAuthorizationService extends EntityService<IssueAuthorization> {

	void authorize(Issue issue, User user);

    void createOrUpdate(IssueAuthorization authorization);
	
}