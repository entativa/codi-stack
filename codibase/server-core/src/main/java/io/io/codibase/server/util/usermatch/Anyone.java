package io.codibase.server.util.usermatch;

import io.codibase.server.model.User;

public class Anyone implements UserMatchCriteria {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean matches(User user) {
		return true;
	}

	@Override
	public String toString() {
		return "anyone";
	}
}
