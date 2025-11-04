package io.codibase.server.buildspec.job;

import java.util.Collection;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.commons.loader.ExtensionPoint;
import io.codibase.server.model.Project;

@ExtensionPoint
public interface JobSuggestion {
	
	Collection<Job> suggestJobs(Project project, ObjectId commitId);

}
