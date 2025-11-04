package io.codibase.server.service;

import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequestQueryPersonalization;
import io.codibase.server.model.User;

public interface PullRequestQueryPersonalizationService extends EntityService<PullRequestQueryPersonalization> {
	
	PullRequestQueryPersonalization find(Project project, User user);

    void createOrUpdate(PullRequestQueryPersonalization personalization);
	
    void delete(PullRequestQueryPersonalization personalization);
}
