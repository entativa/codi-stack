package io.codibase.server.model.support;

import java.util.ArrayList;

import org.jspecify.annotations.Nullable;
import javax.persistence.MappedSuperclass;

import io.codibase.server.model.Project;
import io.codibase.server.model.User;
import io.codibase.server.util.watch.QuerySubscriptionSupport;
import io.codibase.server.util.watch.QueryWatchSupport;

@MappedSuperclass
public interface QueryPersonalization<T extends NamedQuery> {

	@Nullable
	Project getProject();
	
	User getUser();
	
	ArrayList<T> getQueries();
	
	void setQueries(ArrayList<T> userQueries);

	@Nullable
	QueryWatchSupport<T> getQueryWatchSupport();

	@Nullable
	QuerySubscriptionSupport<T> getQuerySubscriptionSupport();
	
	void onUpdated();
	
}
