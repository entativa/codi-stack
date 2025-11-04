package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class TargetBranchCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final String branch;
	
	private final int operator;
	
	public TargetBranchCriteria(String value, int operator) {
		this.branch = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<String> attribute = from.get(PullRequest.PROP_TARGET_BRANCH);
		String normalized = branch.toLowerCase().replace("*", "%");
		var predicate = builder.like(builder.lower(attribute), normalized);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = WildcardUtils.matchString(branch.toLowerCase(), request.getTargetBranch().toLowerCase());
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_TARGET_BRANCH) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(branch);
	}

}
