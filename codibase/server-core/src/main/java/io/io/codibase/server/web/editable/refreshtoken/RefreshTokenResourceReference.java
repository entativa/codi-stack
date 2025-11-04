package io.codibase.server.web.editable.refreshtoken;

import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class RefreshTokenResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public RefreshTokenResourceReference() {
		super(RefreshTokenResourceReference.class, "refresh-token.js");
	}

}
