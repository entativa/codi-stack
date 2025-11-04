package io.codibase.server.buildspec.job;

import org.jspecify.annotations.Nullable;

import io.codibase.server.buildspec.ParamSpecAware;

public interface JobAware extends ParamSpecAware {
	
	@Nullable
	Job getJob();
	
}
