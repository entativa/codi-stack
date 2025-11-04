package io.codibase.server.imports;

import java.io.Serializable;
import java.util.List;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.web.component.taskbutton.TaskResult;
import io.codibase.server.web.util.ImportStep;

public interface IssueImporter extends Serializable {

	String getName();

	List<ImportStep<? extends Serializable>> getSteps();
	
	public abstract TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger);
	
}
