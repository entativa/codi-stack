package io.codibase.server.exception;

import io.codibase.commons.utils.ExplicitException;

public class DataTooLargeException extends ExplicitException {
	public DataTooLargeException(long maxSize) {
		super("Data exceeds maximum size: " + maxSize);
	}
}
