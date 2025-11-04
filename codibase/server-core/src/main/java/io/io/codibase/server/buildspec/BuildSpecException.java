package io.codibase.server.buildspec;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.util.Path;

public class BuildSpecException extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public BuildSpecException(Path path, String message) {
		super(path.toString() + ": " + message);
	}
	
}
