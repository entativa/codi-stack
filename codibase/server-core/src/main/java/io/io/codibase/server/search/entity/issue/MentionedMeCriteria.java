package io.codibase.server.search.entity.issue;

import static io.codibase.server.web.translation.Translation._T;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueMention;
import io.codibase.server.model.User;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class MentionedMeCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (User.get() != null) {
			Subquery<IssueMention> mentionQuery = query.subquery(IssueMention.class);
			Root<IssueMention> mention = mentionQuery.from(IssueMention.class);
			mentionQuery.select(mention);
			mentionQuery.where(builder.and(
					builder.equal(mention.get(IssueMention.PROP_ISSUE), from),
					builder.equal(mention.get(IssueMention.PROP_USER), User.get())));
			return builder.exists(mentionQuery);
		} else {
			throw new ExplicitException(_T("Please login to perform this query"));
		}
	}

	@Override
	public boolean matches(Issue issue) {
		if (User.get() != null)
			return issue.getMentions().stream().anyMatch(it->it.getUser().equals(User.get()));
		else
			throw new ExplicitException(_T("Please login to perform this query"));
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.MentionedMe);
	}

}
