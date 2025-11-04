package io.codibase.server.exception.handler;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.exception.HttpResponse;

import javax.servlet.http.HttpServletResponse;

public class ExplicitExceptionHandler extends AbstractExceptionHandler<ExplicitException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(ExplicitException exception) {
		return new HttpResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
    }
    
}
