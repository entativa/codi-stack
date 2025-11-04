package io.codibase.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.model.Issue;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class DescriptionCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final String value;
	
	public DescriptionCriteria(String value) {
		this.value = value;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Expression<String> attribute = from.get(Issue.PROP_DESCRIPTION);
		return builder.like(builder.lower(attribute), "%" + value.toLowerCase().replace('*', '%') + "%");
	}

	@Override
	public boolean matches(Issue issue) {
		String description = issue.getDescription();
		return description != null && WildcardUtils.matchString("*" + value.toLowerCase() + "*", description.toLowerCase());
	}

	@Override
	public String toStringWithoutParens() {
		return quote(Issue.NAME_DESCRIPTION) + " " 
				+ IssueQuery.getRuleName(IssueQueryLexer.Contains) + " " 
				+ quote(value);
	}

}
