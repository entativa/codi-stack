package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.BaseAuthorization;
import io.codibase.server.model.Project;
import io.codibase.server.model.Role;

public interface BaseAuthorizationService extends EntityService<BaseAuthorization> {

	void syncRoles(Project project, Collection<Role> roles);
			
	void create(BaseAuthorization authorization);
	
}
