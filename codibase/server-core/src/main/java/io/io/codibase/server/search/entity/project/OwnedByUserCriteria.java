package io.codibase.server.search.entity.project;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.GroupAuthorization;
import io.codibase.server.model.Project;
import io.codibase.server.model.Role;
import io.codibase.server.model.User;
import io.codibase.server.model.UserAuthorization;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class OwnedByUserCriteria extends OwnedByCriteria {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public OwnedByUserCriteria(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Join<?, ?> userAuthorizationJoin = from.join(Project.PROP_USER_AUTHORIZATIONS, JoinType.LEFT);

		userAuthorizationJoin.on(builder.and(
				builder.equal(userAuthorizationJoin.get(UserAuthorization.PROP_ROLE), Role.OWNER_ID), 
				builder.equal(userAuthorizationJoin.get(UserAuthorization.PROP_USER), user)));
		
		if (user.getGroups().isEmpty()) {
			return userAuthorizationJoin.isNotNull();
		} else {
			Join<?, ?> groupAuthorizationJoin = from.join(Project.PROP_GROUP_AUTHORIZATIONS, JoinType.LEFT);
			groupAuthorizationJoin.on(builder.and(
					builder.equal(groupAuthorizationJoin.get(GroupAuthorization.PROP_ROLE), Role.OWNER_ID), 
					groupAuthorizationJoin.get(GroupAuthorization.PROP_GROUP).in(user.getGroups())));
			return builder.or(userAuthorizationJoin.isNotNull(), groupAuthorizationJoin.isNotNull());
		}
	}

	@Override
	public boolean matches(Project project) {
		for (UserAuthorization authorization: project.getUserAuthorizations()) {
			if (authorization.getUser().equals(user) && authorization.getRole().isOwner())
				return true;
		}
		for (GroupAuthorization authorization: project.getGroupAuthorizations()) {
			if (authorization.getRole().isOwner() && authorization.getGroup().getMembers().contains(user))
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedBy) + " " + Criteria.quote(user.getName());
	}

}
