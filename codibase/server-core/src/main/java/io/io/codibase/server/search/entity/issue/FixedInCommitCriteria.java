package io.codibase.server.search.entity.issue;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.criteria.Criteria;

public class FixedInCommitCriteria extends Criteria<Issue> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final String value;
	
	private transient ProjectScopedCommit commit;
	
	public FixedInCommitCriteria(@Nullable Project project, String value) {
		this.project = project;
		this.value = value;
	}

	public FixedInCommitCriteria(ProjectScopedCommit commit) {
		this.commit = commit;
		project = commit.getProject();
		value = project.getPath() + ":" + commit.toString();
	}
	
	private ProjectScopedCommit getCommit() {
		if (commit == null)
			commit = EntityQuery.getCommitId(project, value);
		return commit;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<Issue, Issue> from, CriteriaBuilder builder) {
		if (!getCommit().getFixedIssueIds().isEmpty()) 
			return from.get(Issue.PROP_ID).in(getCommit().getFixedIssueIds());
		else 
			return builder.disjunction();
	}

	@Override
	public boolean matches(Issue issue) {
		return getCommit().getFixedIssueIds().contains(issue.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return IssueQuery.getRuleName(IssueQueryLexer.FixedInCommit) + " " + quote(value);
	}

}
