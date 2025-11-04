package io.codibase.server.security.permission;

import io.codibase.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;
import org.jetbrains.annotations.Nullable;

public class AccessBuild implements BasePermission {

	@Override
	public boolean implies(Permission p) {
		return p instanceof AccessBuild;
	}

	@Override
	public boolean isApplicable(@Nullable UserFacade user) {
		return true;
	}
	
}
