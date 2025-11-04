package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.PullRequestCommentRevision;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.PullRequestCommentRevisionService;

@Singleton
public class DefaultPullRequestCommentRevisionService extends BaseEntityService<PullRequestCommentRevision>
        implements PullRequestCommentRevisionService {

    @Transactional
    @Override
    public void create(PullRequestCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 