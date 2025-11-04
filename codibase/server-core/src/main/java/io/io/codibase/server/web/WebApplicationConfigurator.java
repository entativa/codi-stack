package io.codibase.server.web;

import org.apache.wicket.protocol.http.WebApplication;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface WebApplicationConfigurator {

	void configure(WebApplication application);
	
}
