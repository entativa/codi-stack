package io.codibase.server.buildspec;

import java.util.List;

import io.codibase.server.buildspec.param.spec.ParamSpec;

public interface ParamSpecAware {

	List<ParamSpec> getParamSpecs();
	
}
