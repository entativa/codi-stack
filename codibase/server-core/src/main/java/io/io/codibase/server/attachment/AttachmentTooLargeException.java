package io.codibase.server.attachment;

import io.codibase.server.exception.HttpResponseAwareException;

import javax.servlet.http.HttpServletResponse;

public class AttachmentTooLargeException extends HttpResponseAwareException {

	private static final long serialVersionUID = 1L;

	public AttachmentTooLargeException(String message) {
		super(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, message);
	}

}
