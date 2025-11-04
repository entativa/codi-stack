package io.codibase.server.util.watch;

import java.util.LinkedHashSet;

import io.codibase.server.model.support.NamedQuery;

public abstract class QuerySubscriptionSupport<T extends NamedQuery> {

	public abstract LinkedHashSet<String> getQuerySubscriptions();
	
}
