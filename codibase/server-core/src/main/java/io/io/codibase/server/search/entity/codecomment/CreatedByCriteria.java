package io.codibase.server.search.entity.codecomment;

import io.codibase.server.model.CodeComment;
import io.codibase.server.model.User;
import io.codibase.server.util.criteria.Criteria;

public abstract class CreatedByCriteria extends Criteria<CodeComment> {

	public abstract User getUser();
	
}
