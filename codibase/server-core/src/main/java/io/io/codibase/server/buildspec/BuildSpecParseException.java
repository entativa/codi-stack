package io.codibase.server.buildspec;

import io.codibase.commons.utils.ExplicitException;

public class BuildSpecParseException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public BuildSpecParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
