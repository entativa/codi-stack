package io.codibase.server.search.entity.issue;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Build;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class FixedInBuildCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private transient Build build;
	
	public FixedInBuildCriteria(@Nullable Project project, String value) {
		this.project = project;
		this.value = value;
	}

	public FixedInBuildCriteria(Build build) {
		this.build = build;
		project = build.getProject();
		value = build.getReference().toString(null);
	}
	
	private Build getBuild() {
		if (build == null)
			build = EntityQuery.getBuild(project, value);
		return build;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		Collection<Long> fixedIssueIds = getBuild().getFixedIssueIds();
		if (!fixedIssueIds.isEmpty()) 
			return from.get(Issue.PROP_ID).in(fixedIssueIds);
		else 
			return builder.disjunction();
	}

	@Override
	public boolean matches(Issue issue) {
		return getBuild().getFixedIssueIds().contains(issue.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInBuild) + " " + quote(value);
	}

}
