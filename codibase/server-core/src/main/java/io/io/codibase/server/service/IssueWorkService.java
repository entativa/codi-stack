package io.codibase.server.service;

import java.util.Date;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.apache.shiro.subject.Subject;

import io.codibase.server.model.Issue;
import io.codibase.server.model.IssueWork;
import io.codibase.server.model.User;
import io.codibase.server.search.entity.EntityQuery;
import io.codibase.server.util.ProjectScope;

public interface IssueWorkService extends EntityService<IssueWork> {
	
	void createOrUpdate(IssueWork work);
	
	List<IssueWork> query(User user, Issue issue, Date fromDate, Date toDate);
	
	List<IssueWork> query(Subject subject, @Nullable ProjectScope projectScope, EntityQuery<Issue> issueQuery, Date fromDate, Date toDate);
	
}
