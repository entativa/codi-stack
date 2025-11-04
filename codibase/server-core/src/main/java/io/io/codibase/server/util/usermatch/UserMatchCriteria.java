package io.codibase.server.util.usermatch;

import java.io.Serializable;

import io.codibase.server.annotation.Editable;
import io.codibase.server.model.User;

@Editable
public interface UserMatchCriteria extends Serializable {
	
	boolean matches(User user);
	
}
