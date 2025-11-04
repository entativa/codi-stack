package io.codibase.server.persistence.exception;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;

import io.codibase.server.exception.HttpResponse;
import io.codibase.server.exception.handler.AbstractExceptionHandler;

public class EntityNotFoundExceptionHandler extends AbstractExceptionHandler<EntityNotFoundException> {
	
	private static final long serialVersionUID = 1L;

	@Override
    public HttpResponse getResponse(EntityNotFoundException exception) {
		var errorMessage = exception.getMessage();
		if (errorMessage == null)
			errorMessage = "Not found";
		return new HttpResponse(HttpServletResponse.SC_NOT_FOUND, errorMessage);
    }
    
}
