package io.codibase.server.service;

import io.codibase.server.model.PackQueryPersonalization;
import io.codibase.server.model.Project;
import io.codibase.server.model.User;

public interface PackQueryPersonalizationService extends EntityService<PackQueryPersonalization> {
	
	PackQueryPersonalization find(Project project, User user);

    void createOrUpdate(PackQueryPersonalization personalization);
	
}
