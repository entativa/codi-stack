package io.codibase.server.exception;

import io.codibase.commons.utils.ExplicitException;

public class PullRequestReviewRejectedException extends ExplicitException {

	public PullRequestReviewRejectedException(String message) {
		super(message);		
	}

}