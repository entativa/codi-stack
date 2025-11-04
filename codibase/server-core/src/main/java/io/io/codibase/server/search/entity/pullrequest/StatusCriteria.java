package io.codibase.server.search.entity.pullrequest;

import io.codibase.server.model.PullRequest;
import io.codibase.server.model.PullRequest.Status;
import io.codibase.server.util.criteria.Criteria;

public abstract class StatusCriteria extends Criteria<PullRequest> {

	public abstract Status getStatus();
}
