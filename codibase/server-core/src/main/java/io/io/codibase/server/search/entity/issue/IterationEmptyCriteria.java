package io.codibase.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueSchedule;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class IterationEmptyCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;
	
	private final int operator;
	
	public IterationEmptyCriteria(int operator) {
		this.operator = operator;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		var predicate = builder.isEmpty(from.get(Issue.PROP_SCHEDULES));
		if (operator == IssueQueryLexer.IsNotEmpty)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(Issue issue) {
		var matches = issue.getSchedules().isEmpty();
		if (operator == IssueQueryLexer.IsNotEmpty)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(IssueSchedule.NAME_ITERATION) + " " + IssueQuery.getRuleName(operator);
	}

}
