package io.codibase.server.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.commons.utils.PlanarRange;
import io.codibase.server.model.CodeComment;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.criteria.Criteria;

public interface CodeCommentService extends EntityService<CodeComment> {
	
	Collection<CodeComment> query(Project project, ObjectId commitId, @Nullable String path);
	
	Collection<CodeComment> query(Project project, ObjectId...commitIds);
	
	Map<CodeComment, PlanarRange> queryInHistory(Project project, ObjectId commitId, String path);
		
	List<CodeComment> query(Project project, @Nullable PullRequest request, 
			EntityQuery<CodeComment> commentQuery, int firstResult, int maxResults);
	
	int count(Project project, @Nullable PullRequest request, Criteria<CodeComment> commentCriteria);

	List<CodeComment> query(User creator, Date fromDate, Date toDate);

	void delete(Collection<CodeComment> comments, Project project);

	void delete(CodeComment comment);
	
    @Nullable
    CodeComment findByUUID(String uuid);

    void create(CodeComment comment);

	void update(CodeComment comment);
	
	Collection<Long> getProjectIds();
	
}