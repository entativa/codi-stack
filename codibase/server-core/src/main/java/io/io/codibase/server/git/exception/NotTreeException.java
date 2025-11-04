package io.codibase.server.git.exception;

import io.codibase.commons.utils.ExplicitException;

public class NotTreeException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public NotTreeException(String message) {
		super(message);
	}
	
}
