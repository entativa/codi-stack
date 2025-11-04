package io.codibase.server.web.util;

import org.jspecify.annotations.Nullable;

import io.codibase.server.search.entity.issue.IssueQuery;

public interface IssueQueryAware {

	@Nullable
	IssueQuery getIssueQuery();
	
}
