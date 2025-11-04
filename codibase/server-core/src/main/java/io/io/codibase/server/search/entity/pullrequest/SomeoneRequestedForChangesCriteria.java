package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequestReview;
import io.codibase.server.model.PullRequestReview.Status;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class SomeoneRequestedForChangesCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Subquery<PullRequestReview> reviewQuery = query.subquery(PullRequestReview.class);
		Root<PullRequestReview> review = reviewQuery.from(PullRequestReview.class);
		reviewQuery.select(review);
		reviewQuery.where(builder.and(
				builder.equal(review.get(PullRequestReview.PROP_STATUS), Status.REQUESTED_FOR_CHANGES),
				builder.equal(review.get(PullRequestReview.PROP_REQUEST), from)));
		return builder.exists(reviewQuery);
	}

	@Override
	public boolean matches(PullRequest request) {
		for (PullRequestReview review: request.getReviews()) {
			if (review.getStatus() == Status.REQUESTED_FOR_CHANGES)
				return true;
		}
		return false;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.SomeoneRequestedForChanges);
	}

}
