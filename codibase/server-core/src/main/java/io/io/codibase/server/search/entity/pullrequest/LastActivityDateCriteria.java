package io.codibase.server.search.entity.pullrequest;

import java.util.Date;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.support.LastActivity;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class LastActivityDateCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final int operator;
	
	private final String value;
	
	private final Date date;
	
	public LastActivityDateCriteria(String value, int operator) {
		this.operator = operator;
		this.value = value;
		date = EntityQuery.getDateValue(value);
	}

	public int getOperator() {
		return operator;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Date> attribute = PullRequestQuery.getPath(from, PullRequest.PROP_LAST_ACTIVITY + "." + LastActivity.PROP_DATE);
		if (operator == PullRequestQueryLexer.IsUntil)
			return builder.lessThan(attribute, date);
		else
			return builder.greaterThan(attribute, date);
	}

	@Override
	public boolean matches(PullRequest request) {
		if (operator == PullRequestQueryLexer.IsUntil)
			return request.getLastActivity().getDate().before(date);
		else
			return request.getLastActivity().getDate().after(date);
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_LAST_ACTIVITY_DATE) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(value);
	}

}
