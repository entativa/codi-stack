package io.codibase.server.service;

import io.codibase.server.model.Membership;
import io.codibase.server.model.User;

import java.util.Collection;
import java.util.List;

public interface MembershipService extends EntityService<Membership> {
	
	void delete(Collection<Membership> memberships);

	void syncMemberships(User user, Collection<String> groupNames);

    void create(Membership membership);

    List<User> queryMembers(User user);
	
}
