package io.codibase.server.service;

import io.codibase.server.model.Group;
import io.codibase.server.model.GroupAuthorization;
import io.codibase.server.model.Project;

import java.util.Collection;

public interface GroupAuthorizationService extends EntityService<GroupAuthorization> {

	void syncAuthorizations(Group group, Collection<GroupAuthorization> authorizations);

	void syncAuthorizations(Project project, Collection<GroupAuthorization> authorizations);
	
    void createOrUpdate(GroupAuthorization authorization);
	
}
