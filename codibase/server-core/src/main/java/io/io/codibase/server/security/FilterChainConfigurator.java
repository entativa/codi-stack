package io.codibase.server.security;

import org.apache.shiro.web.filter.mgt.FilterChainManager;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface FilterChainConfigurator {
	void configure(FilterChainManager filterChainManager);
}
