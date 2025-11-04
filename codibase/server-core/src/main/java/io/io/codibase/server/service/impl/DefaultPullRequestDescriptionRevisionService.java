package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.PullRequestDescriptionRevision;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.PullRequestDescriptionRevisionService;

@Singleton
public class DefaultPullRequestDescriptionRevisionService extends BaseEntityService<PullRequestDescriptionRevision>
        implements PullRequestDescriptionRevisionService {

    @Transactional
    @Override
    public void create(PullRequestDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 