package io.codibase.server.event.project.issue;

import io.codibase.server.CodiBase;
import io.codibase.server.event.project.ProjectEvent;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.persistence.dao.Dao;
import io.codibase.server.security.SecurityUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class IssuesCopied extends ProjectEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final Long sourceProjectId;
	
	private final Map<Long, Long> issueIdMapping;
	
	public IssuesCopied(Project sourceProject, Project targetProject, Map<Issue, Issue> issueMapping) {
		super(SecurityUtils.getUser(), new Date(), targetProject);
		sourceProjectId = sourceProject.getId();
		issueIdMapping = new HashMap<>();
		for (var entry: issueMapping.entrySet())
			issueIdMapping.put(entry.getKey().getId(), entry.getValue().getId());
	}

	public Project getSourceProject() {
		return CodiBase.getInstance(Dao.class).load(Project.class, sourceProjectId);
	}

	public Project getTargetProject() {
		return getProject();
	}
	
	public Map<Long, Long> getIssueIdMapping() {
		return issueIdMapping;
	}

	@Override
	public String getActivity() {
		return "copied";
	}
	
}
