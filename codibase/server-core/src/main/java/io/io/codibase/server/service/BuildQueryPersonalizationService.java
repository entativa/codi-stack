package io.codibase.server.service;

import io.codibase.server.model.BuildQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;

public interface BuildQueryPersonalizationService extends EntityService<BuildQueryPersonalization> {
	
	BuildQueryPersonalization find(Project project, User user);

    void createOrUpdate(BuildQueryPersonalization buildQueryPersonalization);
	
}
