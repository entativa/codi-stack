package io.codibase.server.service;

import io.codibase.server.model.PullRequestAssignment;

public interface PullRequestAssignmentService extends EntityService<PullRequestAssignment> {

    void create(PullRequestAssignment assignment);
}
