package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.support.pullrequest.MergePreview;
import io.codibase.server.util.ProjectScope;
import io.codibase.server.util.criteria.Criteria;

public class HasMergeConflictsCriteria extends Criteria<PullRequest> {

	private static final long serialVersionUID = 1L;

	@Override
	public Predicate getPredicate(@Nullable ProjectScope projectScope, CriteriaQuery<?> query, From<PullRequest, PullRequest> from, CriteriaBuilder builder) {
		Path<?> requestHead = PullRequestQuery.getPath(from, PullRequest.PROP_MERGE_PREVIEW + "." + MergePreview.PROP_HEAD_COMMIT_HASH);
		Path<?> merged = PullRequestQuery.getPath(from, PullRequest.PROP_MERGE_PREVIEW + "." + MergePreview.PROP_MERGED_COMMIT_HASH);
		return builder.and(
				builder.isNotNull(requestHead), 
				builder.isNull(merged));
	}

	@Override
	public boolean matches(PullRequest request) {
		MergePreview preview = request.getMergePreview();
		return preview != null && preview.getMergeCommitHash() == null;
	}

	@Override
	public String toStringWithoutParens() {
		return PullRequestQuery.getRuleName(PullRequestQueryLexer.HasMergeConflicts);
	}

}
