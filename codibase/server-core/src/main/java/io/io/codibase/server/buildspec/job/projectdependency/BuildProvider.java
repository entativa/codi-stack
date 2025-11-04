package io.codibase.server.buildspec.job.projectdependency;

import java.io.Serializable;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.annotation.Editable;

@Editable
public interface BuildProvider extends Serializable {

	@Nullable
	Build getBuild(Project project);
	
	String getDescription();
}
