package io.codibase.server.web.component.commandpalette;

import io.codibase.commons.utils.ExplicitException;

public class IgnoredUrlParam extends ExplicitException {

	private static final long serialVersionUID = 1L;

	public IgnoredUrlParam(String message) {
		super(message);
	}

}
