package io.codibase.server.service;

import io.codibase.server.model.CommitQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;

public interface CommitQueryPersonalizationService extends EntityService<CommitQueryPersonalization> {
	
	CommitQueryPersonalization find(Project project, User user);

    void createOrUpdate(CommitQueryPersonalization commitQueryPersonalization);
	
}
