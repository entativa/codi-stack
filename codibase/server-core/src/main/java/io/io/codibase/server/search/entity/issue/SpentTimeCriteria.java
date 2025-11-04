package io.codibase.server.search.entity.issue;

import static io.codibase.server.model.Issue.NAME_SPENT_TIME;
import static io.codibase.server.model.Issue.PROP_TOTAL_SPENT_TIME;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.Issue;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;


public class SpentTimeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final int value;
	
	private final int operator;
	
	public SpentTimeCriteria(int value, int operator) {
		this.value = value;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Path<Integer> spentTimeAttribute = from.get(PROP_TOTAL_SPENT_TIME);
		if (operator == IssueQueryLexer.Is)
			return builder.equal(spentTimeAttribute, value);
		else if (operator == IssueQueryLexer.IsNot)
			return builder.not(builder.equal(spentTimeAttribute, value));
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return builder.greaterThan(spentTimeAttribute, value);
		else
			return builder.lessThan(spentTimeAttribute, value);
	}

	@Override
	public boolean matches(Issue issue) {
		if (operator == IssueQueryLexer.Is)
			return issue.getTotalSpentTime() == value;
		else if (operator == IssueQueryLexer.IsNot)
			return issue.getTotalSpentTime() != value;			
		else if (operator == IssueQueryLexer.IsGreaterThan)
			return issue.getTotalSpentTime() > value;
		else
			return issue.getTotalSpentTime() < value;
	}

	@Override
	public String toStringWithoutParens() {
		var timeTrackingSetting = CodiBase.getInstance(SettingService.class).getIssueSetting().getTimeTrackingSetting();
		return quote(NAME_SPENT_TIME) + " "
				+ IssueQuery.getRuleName(operator) + " "
				+ quote(timeTrackingSetting.formatWorkingPeriod(value, false));
	}

	@Override
	public void fill(Issue issue) {
	}

}
