package io.codibase.server.exception;

import io.codibase.commons.utils.ExplicitException;

public class NoSubscriptionException extends ExplicitException {
	
	public NoSubscriptionException(String feature) {
		super(feature + " requires an active subscription");
	}
	
}
