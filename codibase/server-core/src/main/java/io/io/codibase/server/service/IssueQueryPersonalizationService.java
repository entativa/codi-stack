package io.codibase.server.service;

import io.codibase.server.model.IssueQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.util.ProjectScope;

import java.util.Collection;

public interface IssueQueryPersonalizationService extends EntityService<IssueQueryPersonalization> {
	
	IssueQueryPersonalization find(Project project, User user);

    void createOrUpdate(IssueQueryPersonalization personalization);
	
	Collection<IssueQueryPersonalization> query(ProjectScope projectScope);
	
}
