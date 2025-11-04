package io.codibase.server.service.impl;

import java.util.Collection;
import java.util.List;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.BaseAuthorization;
import io.codibase.server.model.Project;
import io.codibase.server.model.Role;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.BaseAuthorizationService;

@Singleton
public class DefaultBaseAuthorizationService extends BaseEntityService<BaseAuthorization>
		implements BaseAuthorizationService {

	@Transactional
	@Override
	public void syncRoles(Project project, Collection<Role> roles) {
		for (var it = project.getBaseAuthorizations().iterator(); it.hasNext();) {
			var authorization = it.next();
			var found = false;
			for (var newRole: roles) {
				if (newRole.equals(authorization.getRole())) {
					found = true;
					break;
				}
			}
			if (!found) {
				it.remove();
				delete(authorization);
			}
		}

		for (var newRole: roles) {
			var found = false;
			for (var authorization: project.getBaseAuthorizations()) {
				if (authorization.getRole().equals(newRole)) {
					found = true;
					break;
				}
			}
			if (!found) {
				var authorization = new BaseAuthorization();
				authorization.setProject(project);
				authorization.setRole(newRole);
				project.getBaseAuthorizations().add(authorization);
				dao.persist(authorization);
			}
		}
	}
	
	@Transactional
	@Override
	public void create(BaseAuthorization authorization) {
		Preconditions.checkArgument(authorization.isNew());
		dao.persist(authorization);
	}

	@Override
	public List<BaseAuthorization> query() {
		return query(true);
	}

	@Override
	public int count() {
		return count(true);
	}
	
}
