package io.codibase.server.buildspec.job.action.condition;

import java.util.List;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Build;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class ParamEmptyCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String name;
	
	private final int operator;
	
	public ParamEmptyCriteria(String name, int operator) {
		this.name = name;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		List<String> paramValues = build.getParamMap().get(name);
		var matches = paramValues == null || paramValues.isEmpty();
		if (operator == ActionConditionLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(name) + " " + ActionCondition.getRuleName(operator);
	}
	
}
