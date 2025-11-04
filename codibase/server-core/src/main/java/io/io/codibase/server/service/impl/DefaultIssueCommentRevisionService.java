package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.IssueCommentRevision;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.IssueCommentRevisionService;

@Singleton
public class DefaultIssueCommentRevisionService extends BaseEntityService<IssueCommentRevision>
        implements IssueCommentRevisionService {

    @Transactional
    @Override
    public void create(IssueCommentRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
} 