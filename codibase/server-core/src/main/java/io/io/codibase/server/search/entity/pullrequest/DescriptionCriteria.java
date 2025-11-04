package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class DescriptionCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Expression<String> attribute = from.get(PullRequest.PROP_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(PullRequest request) {
		String description = request.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description.toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_DESCRIPTION) + " " 
				+ PullRequestQuery.getRuleName(PullRequestQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
