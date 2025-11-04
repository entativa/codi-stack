package io.codibase.server.jetty;

import org.eclipse.jetty.server.Server;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ServerConfigurator {
	void configure(Server server);
}
