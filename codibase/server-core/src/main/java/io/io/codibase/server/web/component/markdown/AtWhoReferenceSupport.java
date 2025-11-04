package io.codibase.server.web.component.markdown;

import io.codibase.server.model.Build;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;

import java.io.Serializable;
import java.util.List;

public interface AtWhoReferenceSupport extends Serializable {
	
	Project getCurrentProject();
	
	List<PullRequest> queryPullRequests(Project project, String query, int count);

	List<Issue> queryIssues(Project project, String query, int count);
	
	List<Build> queryBuilds(Project project, String query, int count);
	
}
