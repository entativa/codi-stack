package io.codibase.server.exception;

import io.codibase.commons.utils.ExplicitException;

public class ServerNotReadyException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public ServerNotReadyException() {
		super("Server not ready");
	}

}
