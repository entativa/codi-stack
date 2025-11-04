package io.codibase.server.service;

import io.codibase.server.model.AccessToken;
import io.codibase.server.model.User;

import org.jspecify.annotations.Nullable;

public interface AccessTokenService extends EntityService<AccessToken> {

	@Nullable
	AccessToken findByOwnerAndName(User owner, String name);
	
	@Nullable
    AccessToken findByValue(String value);

	void createOrUpdate(AccessToken projectToken);

	String createTemporal(Long userId, long secondsToExpire);
	
}
