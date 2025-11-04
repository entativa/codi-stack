package io.codibase.server.search.entitytext;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;

public interface PullRequestTextService {

	List<Long> query(@Nullable Project project, String queryString, int count);
			
	boolean matches(PullRequest request, String queryString);

}
