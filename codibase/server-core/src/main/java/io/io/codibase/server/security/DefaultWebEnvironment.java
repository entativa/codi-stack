package io.codibase.server.security;

import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.config.ShiroFilterConfiguration;
import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;

import io.codibase.server.CodiBase;

public class DefaultWebEnvironment extends org.apache.shiro.web.env.DefaultWebEnvironment implements Initializable {

	@Override
	public void init() throws ShiroException {
		setWebSecurityManager(CodiBase.getInstance(WebSecurityManager.class));
		setFilterChainResolver(CodiBase.getInstance(FilterChainResolver.class));
		setShiroFilterConfiguration(CodiBase.getInstance(ShiroFilterConfiguration.class));
	}

}
