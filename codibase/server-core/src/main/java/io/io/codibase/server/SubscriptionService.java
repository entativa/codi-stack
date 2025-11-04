package io.codibase.server;

import io.codibase.server.annotation.NoDBAccess;
import org.apache.wicket.Component;

import org.jspecify.annotations.Nullable;

public interface SubscriptionService {

	@NoDBAccess
	boolean isSubscriptionActive();
	
	@Nullable
	String getLicensee();

	Component renderSupportRequestLink(String componentId);
	
}
