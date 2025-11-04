package io.codibase.server.service;

import io.codibase.server.model.PullRequestDescriptionRevision;

public interface PullRequestDescriptionRevisionService extends EntityService<PullRequestDescriptionRevision> {
		
	void create(PullRequestDescriptionRevision revision);
	
}
