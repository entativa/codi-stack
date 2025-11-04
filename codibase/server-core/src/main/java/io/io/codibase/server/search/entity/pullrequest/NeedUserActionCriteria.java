package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.AndCriteria;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.util.criteria.OrCriteria;

public class NeedUserActionCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final User user;
	
	public NeedUserActionCriteria(User user) {
		this.user = user;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		return getCriteria().getPredicate(projectScope, query, from, builder);
	}

	@Override
	public boolean matches(PullRequest request) {
		return getCriteria().matches(request);
	}
	
	@SuppressWarnings("unchecked")
	private Criteria<PullRequest> getCriteria() {
		return new OrCriteria<>(
				new ToBeReviewedByUserCriteria(user),
				new ToBeChangedByUserCriteria(user),
				new ToBeMergedByUserCriteria(user),
				new AndCriteria<>(new SubmittedByUserCriteria(user), new HasUnsuccessfulBuilds())
				);
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.NeedActionOf) + " " + quote(user.getName());
	}

}
