package io.codibase.server.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.shiro.authz.UnauthenticatedException;

import io.codibase.server.service.SettingService;
import io.codibase.server.rest.resource.TriggerJobResource;
import io.codibase.server.rest.annotation.Api;
import io.codibase.server.security.SecurityUtils;

@Provider
public class AnonymousCheckFilter implements ContainerRequestFilter {

	private final SettingService settingService;
	
	@Context
	private ResourceInfo resourceInfo;
	
	@Context
	private HttpServletRequest request;
	
	@Inject
	public AnonymousCheckFilter(SettingService settingService) {
		this.settingService = settingService;
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		Api api = resourceInfo.getResourceClass().getAnnotation(Api.class);
		if ((api == null || !api.internal()) && SecurityUtils.isAnonymous() 
				&& resourceInfo.getResourceClass() != TriggerJobResource.class) { 
			String method = request.getMethod();
			if (method.equals("POST") || method.equals("DELETE") || method.equals("PUT") 
					|| !settingService.getSecuritySetting().isEnableAnonymousAccess()) {
				throw new UnauthenticatedException();
			}
		}
	}

}
