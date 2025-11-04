package io.codibase.server.service;

import java.util.Collection;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.search.buildmetric.BuildMetricQuery;

public interface BuildMetricService {
	
	@Nullable
	<T extends AbstractEntity> T find(Class<T> metricClass, Build build, String reportName);
	
	<T extends AbstractEntity> Map<Integer, T> queryStats(Project project, Class<T> metricClass, BuildMetricQuery query);
	
	Map<String, Collection<String>> getAccessibleReportNames(Project project, Class<?> metricClass);
	
}
