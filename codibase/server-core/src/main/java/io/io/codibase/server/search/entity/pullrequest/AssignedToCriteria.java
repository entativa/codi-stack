package io.codibase.server.search.entity.pullrequest;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.util.criteria.Criteria;

public abstract class AssignedToCriteria extends Criteria<PullRequest> {

    @Nullable
    public abstract User getUser();
    
}
