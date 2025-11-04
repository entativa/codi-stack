package io.codibase.server.job;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.terminal.Shell;
import io.codibase.server.terminal.Terminal;

import java.io.Serializable;

public interface JobRunnable extends Serializable {
	
	boolean run(TaskLogger jobLogger);

	void resume(JobContext jobContext);

	Shell openShell(JobContext jobContext, Terminal terminal);
	
}
