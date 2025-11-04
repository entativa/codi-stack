package io.codibase.server.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.server.service.support.AuditQuery;
import io.codibase.server.model.Audit;
import io.codibase.server.model.Project;

public interface AuditService extends EntityService<Audit> {
		
	void audit(@Nullable Project project, String action, @Nullable String oldContent, @Nullable String newContent);

	List<Audit> query(@Nullable Project project, AuditQuery query, int firstResult, int maxResults);

	int count(@Nullable Project project, AuditQuery query);
	
}
