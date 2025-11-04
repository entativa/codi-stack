package io.codibase.server.service.impl;

import java.util.Collection;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.LinkAuthorization;
import io.codibase.server.model.LinkSpec;
import io.codibase.server.model.Role;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.LinkAuthorizationService;

@Singleton
public class DefaultLinkAuthorizationService extends BaseEntityService<LinkAuthorization> implements LinkAuthorizationService {

	@Transactional
	@Override
	public void syncAuthorizations(Role role, Collection<LinkSpec> authorizedLinks) {
		for (LinkAuthorization authorization: role.getLinkAuthorizations()) {
			if (!authorizedLinks.contains(authorization.getLink()))
				delete(authorization);
		}

		for (LinkSpec link: authorizedLinks) {
			boolean found = false;
			for (LinkAuthorization authorization: role.getLinkAuthorizations()) {
				if (authorization.getLink().equals(link)) {
					found = true;
					break;
				}
			}
			if (!found) {
				LinkAuthorization authorization = new LinkAuthorization();
				authorization.setLink(link);
				authorization.setRole(role);
				create(authorization);
			}
		}
	}

	@Transactional
	@Override
	public void create(LinkAuthorization authorization) {
		Preconditions.checkState(authorization.isNew());
		dao.persist(authorization);
	}

}
