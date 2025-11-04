package io.codibase.server.exception;

import io.codibase.commons.utils.ExplicitException;

public class DigestNotMatchException extends ExplicitException {
	
	public DigestNotMatchException() {
		super("Digest not matching");
	}
	
}
