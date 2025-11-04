package io.codibase.server.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServletConfigurator {
	void configure(ServletContextHandler context);
}
