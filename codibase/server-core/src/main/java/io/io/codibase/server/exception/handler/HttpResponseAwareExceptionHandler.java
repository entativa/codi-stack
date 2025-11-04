package io.codibase.server.exception.handler;

import io.codibase.server.exception.HttpResponse;
import io.codibase.server.exception.HttpResponseAwareException;

public class HttpResponseAwareExceptionHandler extends AbstractExceptionHandler<HttpResponseAwareException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(HttpResponseAwareException exception) {
		return exception.getHttpResponse();
    }
    
}
