package io.codibase.server.service;

import io.codibase.server.model.IssueCommentRevision;

public interface IssueCommentRevisionService extends EntityService<IssueCommentRevision> {

    void create(IssueCommentRevision revision);
    
}
