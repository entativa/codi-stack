package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.model.UserAuthorization;

public interface UserAuthorizationService extends EntityService<UserAuthorization> {

	void syncAuthorizations(User user, Collection<UserAuthorization> authorizations);
	
	void syncAuthorizations(Project project, Collection<UserAuthorization> authorizations);
	
    void createOrUpdate(UserAuthorization authorization);
	
}