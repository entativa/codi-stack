package io.codibase.server.plugin.pack.helm;

import io.codibase.server.exception.HttpResponseAwareException;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode, String errorMessage) {
		super(statusCode, errorMessage);
	}

	public ClientException(int statusCode) {
		super(statusCode);
	}
	
}
