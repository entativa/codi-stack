package io.codibase.server.imports;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.web.component.taskbutton.TaskResult;
import io.codibase.server.web.util.ImportStep;

import java.io.Serializable;
import java.util.List;

public interface ProjectImporter extends Serializable {

	String getName();
	
	List<ImportStep<? extends Serializable>> getSteps();

	TaskResult doImport(boolean dryRun, TaskLogger logger);
	
}
