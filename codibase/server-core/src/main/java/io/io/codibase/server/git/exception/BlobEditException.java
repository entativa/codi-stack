package io.codibase.server.git.exception;

import io.codibase.commons.utils.ExplicitException;

public class BlobEditException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public BlobEditException(String message) {
		super(message);
	}

}
