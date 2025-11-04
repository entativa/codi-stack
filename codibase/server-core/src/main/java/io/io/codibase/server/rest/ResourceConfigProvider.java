package io.codibase.server.rest;

import javax.inject.Provider;

import org.glassfish.jersey.server.ResourceConfig;

public class ResourceConfigProvider implements Provider<ResourceConfig> {

	@Override
	public ResourceConfig get() {
		return ResourceConfig.forApplicationClass(JerseyApplication.class);
	}

}
