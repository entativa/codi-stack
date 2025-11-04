package io.codibase.server.service;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.PersonIdent;

import io.codibase.server.model.EmailAddress;
import io.codibase.server.model.User;
import io.codibase.server.util.facade.EmailAddressCache;
import io.codibase.server.util.facade.EmailAddressFacade;

public interface EmailAddressService extends EntityService<EmailAddress> {

	@Nullable
	EmailAddress findByValue(String value);
	
	@Nullable
	EmailAddressFacade findFacadeByValue(String value);
	
	@Nullable 
	EmailAddressFacade findPrimaryFacade(Long userId);
	
	@Nullable
	EmailAddress findPrimary(User user);

	@Nullable
	EmailAddress findGit(User user);

	@Nullable
	EmailAddress findPublic(User user);
	
	@Nullable
	EmailAddress findByPersonIdent(PersonIdent personIdent);
	
	void setAsPrimary(EmailAddress emailAddress);

	void setAsPublic(EmailAddress emailAddress);

	void setAsPrivate(EmailAddress emailAddress);

	void useForGitOperations(EmailAddress emailAddress);
	
	void sendVerificationEmail(EmailAddress emailAddress);

	EmailAddressCache cloneCache();

    void create(EmailAddress address);

	void update(EmailAddress address);
	
}