package io.codibase.server.web;

import java.util.Collection;

import io.codibase.commons.loader.ExtensionPoint;

@ExtensionPoint
public interface ResourcePackScopeContribution {
	Collection<Class<?>> getResourcePackScopes();
}
