package io.codibase.server.service;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.SsoAccount;
import io.codibase.server.model.SsoProvider;

public interface SsoAccountService extends EntityService<SsoAccount> {
	
	void create(SsoAccount ssoAccount);
		
	@Nullable
	SsoAccount find(SsoProvider provider, String subject);

}
