package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.LinkAuthorization;
import io.codibase.server.model.LinkSpec;
import io.codibase.server.model.Role;

public interface LinkAuthorizationService extends EntityService<LinkAuthorization> {

	void syncAuthorizations(Role role, Collection<LinkSpec> authorizedLinks);

    void create(LinkAuthorization authorization);

}
