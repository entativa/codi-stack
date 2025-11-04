package io.codibase.server.security.permission;

import io.codibase.server.util.facade.UserFacade;
import org.apache.shiro.authz.Permission;

import org.jspecify.annotations.Nullable;

public interface BasePermission extends Permission {
	
	boolean isApplicable(@Nullable UserFacade user);
	
}
