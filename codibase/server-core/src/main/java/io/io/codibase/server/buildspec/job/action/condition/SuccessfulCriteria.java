package io.codibase.server.buildspec.job.action.condition;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Build;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class SuccessfulCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		return build.getStatus() == Build.Status.SUCCESSFUL;
	}

	@Override
	public String toStringWithoutParens() {
		return ActionCondition.getRuleName(ActionConditionLexer.Successful);
	}
	
}
