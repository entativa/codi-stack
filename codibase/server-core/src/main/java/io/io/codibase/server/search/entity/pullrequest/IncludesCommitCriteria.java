package io.codibase.server.search.entity.pullrequest;

import java.util.Collection;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.server.CodiBase;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.ProjectScopedCommit;
import io.codibase.server.util.criteria.Criteria;
import io.codibase.server.xodus.PullRequestInfoService;

public class IncludesCommitCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	private final Project project;
	
	private final ObjectId commitId;
	
	private final String value;
	
	public IncludesCommitCriteria(@Nullable Project project, String value) {
		ProjectScopedCommit commitId = EntityQuery.getCommitId(project, value);
		this.project = commitId.getProject();
		this.commitId = commitId.getCommitId();
		this.value = value;
	}
	
	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Collection<Long> pullRequestIds = getPullRequestIds();
		if (!pullRequestIds.isEmpty()) 
			return from.get(PullRequest.PROP_ID).in(pullRequestIds);
		else 
			return builder.disjunction();
	}
	
	private Collection<Long> getPullRequestIds() {
		return CodiBase.getInstance(PullRequestInfoService.class).getPullRequestIds(project, commitId);
	}
	
	@Override
	public boolean matches(PullRequest request) {
		return getPullRequestIds().contains(request.getId());
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.IncludesCommit) + " " + quote(value);
	}

}
