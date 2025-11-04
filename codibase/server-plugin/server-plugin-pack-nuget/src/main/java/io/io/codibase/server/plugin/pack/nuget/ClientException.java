package io.codibase.server.plugin.pack.nuget;

import io.codibase.server.exception.HttpResponseAwareException;

public class ClientException extends HttpResponseAwareException {

	public ClientException(int statusCode) {
		super(statusCode);
	}
	
}
