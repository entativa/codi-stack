package io.codibase.server.rest;

import org.glassfish.jersey.server.ResourceConfig;

public interface JerseyConfigurator {
	void configure(ResourceConfig resourceConfig);
}
