package io.codibase.server.service;

import io.codibase.server.model.AccessToken;
import io.codibase.server.model.AccessTokenAuthorization;

import java.util.Collection;

public interface AccessTokenAuthorizationService extends EntityService<AccessTokenAuthorization> {

	void syncAuthorizations(AccessToken token, Collection<AccessTokenAuthorization> authorizations);
	
    void createOrUpdate(AccessTokenAuthorization authorization);
	
}