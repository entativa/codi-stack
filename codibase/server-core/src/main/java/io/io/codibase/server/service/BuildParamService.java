package io.codibase.server.service;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.BuildParam;
import io.codibase.server.model.Project;

public interface BuildParamService extends EntityService<BuildParam> {
	
	void create(BuildParam param);
	
	Collection<String> getParamNames(@Nullable Project project);

}
