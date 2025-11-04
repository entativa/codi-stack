package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.commons.utils.match.WildcardUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class TargetProjectCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;
	
	private final String projectPath;
	
	private final int operator;

	public TargetProjectCriteria(String projectPath, int operator) {
		this.projectPath = projectPath;
		this.operator = operator;
	}

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<Project> project = from.join(PullRequest.PROP_TARGET_PROJECT, JoinType.INNER);
		var predicate = CodiBase.getInstance(ProjectService.class).getPathMatchPredicate(builder, project, projectPath);
		if (operator == PullRequestQueryLexer.IsNot)
			predicate = builder.not(predicate);
		return predicate;
	}

	@Override
	public boolean matches(PullRequest request) {
		var matches = WildcardUtils.matchPath(projectPath, request.getTargetProject().getPath());
		if (operator == PullRequestQueryLexer.IsNot)
			matches = !matches;
		return matches;
	}

	@Override
	public String toStringWithoutParens() {
		return quote(PullRequest.NAME_TARGET_PROJECT) + " " 
				+ PullRequestQuery.getRuleName(operator) + " " 
				+ quote(projectPath);
	}

}
