package io.codibase.server.job;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.eclipse.jgit.lib.ObjectId;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.cluster.ClusterTask;
import io.codibase.server.model.Build;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.model.PullRequest;
import io.codibase.server.model.User;
import io.codibase.server.terminal.Shell;
import io.codibase.server.terminal.Terminal;
import io.codibase.server.terminal.WebShell;

public interface JobService {
	
	Build submit(User user, Project project, ObjectId commitId, String jobName,
				 Map<String, List<String>> paramMap, String refName, 
				 @Nullable PullRequest request, @Nullable Issue issue, String reason);
	
	void resubmit(User user, Build build, String reason);
	
	void cancel(Build build);
	
	void resume(Build build);

	boolean runJob(String server, ClusterTask<Boolean> runnable);

	boolean runJob(JobContext jobContext, JobRunnable runnable);
	
	WebShell openShell(Build build, Terminal terminal);
	
	JobContext getJobContext(String jobToken, boolean mustExist);
	
	@Nullable
	Shell getShell(String sessionId);
	
	void reportJobWorkspace(JobContext jobContext, String workspacePath);
	
	@Nullable
	JobContext getJobContext(Long buildId);

	void copyDependencies(JobContext jobContext, File targetDir);
	
	ServerStepResult runServerStep(JobContext jobContext, List<Integer> stepPosition, File inputDir,
								   Map<String, String> placeholderValues, boolean callByAgent,
								   TaskLogger logger);
	
}
