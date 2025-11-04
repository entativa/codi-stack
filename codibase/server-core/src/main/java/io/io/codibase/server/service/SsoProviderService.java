package io.codibase.server.service;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.SsoProvider;

public interface SsoProviderService extends EntityService<SsoProvider> {
	
	void createOrUpdate(SsoProvider ssoProvider);
		
	@Nullable
	SsoProvider find(String name);

}
