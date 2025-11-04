package io.codibase.server.web.util;

import io.codibase.server.model.Project;

import org.jspecify.annotations.Nullable;

public interface ProjectAware {
	
	@Nullable
	Project getProject();
	
}
