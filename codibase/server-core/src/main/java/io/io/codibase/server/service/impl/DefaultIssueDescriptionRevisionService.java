package io.codibase.server.service.impl;

import javax.inject.Singleton;

import com.google.common.base.Preconditions;

import io.codibase.server.model.IssueDescriptionRevision;
import io.codibase.server.persistence.annotation.Transactional;
import io.codibase.server.service.IssueDescriptionRevisionService;

@Singleton
public class DefaultIssueDescriptionRevisionService extends BaseEntityService<IssueDescriptionRevision>
        implements IssueDescriptionRevisionService {

    @Transactional
    @Override
    public void create(IssueDescriptionRevision revision) {
        Preconditions.checkArgument(revision.isNew());
        dao.persist(revision);
    }
    
} 