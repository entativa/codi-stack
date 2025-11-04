package io.codibase.server.service;

import io.codibase.server.model.GitLfsLock;

public interface GitLfsLockService extends EntityService<GitLfsLock> {

	GitLfsLock find(String path);

    void create(GitLfsLock lock);
	
}
