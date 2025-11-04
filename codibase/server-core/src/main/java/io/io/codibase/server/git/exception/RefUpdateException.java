package io.codibase.server.git.exception;

import io.codibase.commons.utils.ExplicitException;
import org.eclipse.jgit.lib.RefUpdate;

public class RefUpdateException extends ExplicitException {

	private static final long serialVersionUID = 1L;
	
	private final RefUpdate.Result result;
	
	public RefUpdateException(RefUpdate.Result result) {
		super(result.name());
		this.result = result;
	}

	public RefUpdate.Result getResult() {
		return result;
	}
	
}
