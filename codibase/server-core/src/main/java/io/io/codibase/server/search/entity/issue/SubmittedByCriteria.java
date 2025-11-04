package io.codibase.server.search.entity.issue;

import io.codibase.server.model.Issue;
import io.codibase.server.model.User;
import io.codibase.server.util.criteria.Criteria;

public abstract class SubmittedByCriteria extends Criteria<Issue> {

	public abstract User getUser();
	
}
