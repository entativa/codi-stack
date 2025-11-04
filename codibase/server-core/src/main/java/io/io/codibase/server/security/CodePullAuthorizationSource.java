package io.codibase.server.security;

import javax.servlet.http.HttpServletRequest;

import io.codibase.server.model.Project;

public interface CodePullAuthorizationSource {

	boolean canPullCode(HttpServletRequest request, Project project);
	
}
