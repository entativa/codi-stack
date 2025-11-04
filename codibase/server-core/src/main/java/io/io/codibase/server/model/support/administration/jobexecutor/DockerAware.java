package io.codibase.server.model.support.administration.jobexecutor;

import io.codibase.k8shelper.RegistryLoginFacade;

import java.util.List;

public interface DockerAware {
	
	List<RegistryLoginFacade> getRegistryLogins(String jobToken);
	
}
