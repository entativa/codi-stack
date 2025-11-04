package io.codibase.server.buildspec.job.action.condition;

import static io.codibase.server.buildspec.job.action.condition.ActionCondition.getRuleName;
import static io.codibase.server.model.Build.NAME_PROJECT;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.model.Build;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class ProjectCriteria extends Criteria<Build> {

	private static final long serialVersionUID = 1L;

	private final String projectPath;

	private final int operator;

	public ProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Build, Build> from, CriteriaBuilder builder) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean matches(Build build) {
		var matches = WildcardUtils.matchPath(projectPath, build.getProject().getPath());
		if (operator == ActionConditionLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(NAME_PROJECT) + " " + getRuleName(operator) + " " + quote(projectPath);
	}
	
}
