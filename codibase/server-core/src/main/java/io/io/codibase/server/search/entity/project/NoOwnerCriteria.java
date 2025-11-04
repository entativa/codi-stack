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
import io.codibase.server.model.UserAuthorization;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class NoOwnerCriteria extends Criteria<Project> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Project, Project> from, CriteriaBuilder builder) {
		Join<?, ?> userAuthorizationJoin = from.join(Project.PROP_USER_AUTHORIZATIONS, JoinType.LEFT);
		Join<?, ?> groupAuthorizationJoin = from.join(Project.PROP_GROUP_AUTHORIZATIONS, JoinType.LEFT);

		userAuthorizationJoin.on(builder.equal(
				userAuthorizationJoin.get(UserAuthorization.PROP_ROLE), Role.OWNER_ID));
		groupAuthorizationJoin.on(
				builder.equal(groupAuthorizationJoin.get(GroupAuthorization.PROP_ROLE), Role.OWNER_ID));
		return builder.and(userAuthorizationJoin.isNull(), groupAuthorizationJoin.isNull());
	}

	@Override
	public boolean matches(Project project) {
		for (UserAuthorization authorization: project.getUserAuthorizations()) {
			if (authorization.getRole().isOwner())
				return false;
		}
		for (GroupAuthorization authorization: project.getGroupAuthorizations()) {
			if (authorization.getRole().isOwner())
				return false;;
		}
		return true;
	}

	@Override
	public String toStringWithoutParens() {
		return ProjectQuery.getRuleName(ProjectQueryLexer.OwnedByNone);
	}

}
