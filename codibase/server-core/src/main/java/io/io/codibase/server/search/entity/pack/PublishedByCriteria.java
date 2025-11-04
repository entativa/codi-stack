package io.codibase.server.search.entity.pack;

import io.codibase.server.model.Pack;
import io.codibase.server.model.User;
import io.codibase.server.util.criteria.Criteria;

public abstract class PublishedByCriteria extends Criteria<Pack> {

	public abstract User getUser();
	
}
