package io.codibase.server.service;

import io.codibase.server.model.PullRequestCommentRevision;

public interface PullRequestCommentRevisionService extends EntityService<PullRequestCommentRevision> {

    void create(PullRequestCommentRevision revision);
    
}
