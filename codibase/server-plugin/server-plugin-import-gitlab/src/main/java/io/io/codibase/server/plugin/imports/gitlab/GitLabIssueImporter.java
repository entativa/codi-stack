package io.codibase.server.plugin.imports.gitlab;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Lists;

import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.CodiBase;
import io.codibase.server.service.ProjectService;
import io.codibase.server.imports.IssueImporter;
import io.codibase.server.persistence.TransactionService;
import io.codibase.server.web.component.taskbutton.TaskResult;
import io.codibase.server.web.component.taskbutton.TaskResult.HtmlMessgae;
import io.codibase.server.web.util.ImportStep;

public class GitLabIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Authenticate to GitLab");
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	private final ImportStep<ImportProject> projectStep = new ImportStep<ImportProject>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Choose project");
		}

		@Override
		protected ImportProject newSetting() {
			ImportProject project = new ImportProject();
			project.server = serverStep.getSetting();
			return project;
		}
		
	};
	
	private final ImportStep<IssueImportOption> optionStep = new ImportStep<IssueImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Specify import option");
		}

		@Override
		protected IssueImportOption newSetting() {
			return serverStep.getSetting().buildIssueImportOption(
					Lists.newArrayList(projectStep.getSetting().getProject()));
		}
		
	};
	
	@Override
	public String getName() {
		return GitLabModule.NAME;
	}
	
	@Override
	public TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger) {
		return CodiBase.getInstance(TransactionService.class).call(() -> {
			var project = CodiBase.getInstance(ProjectService.class).load(projectId);
			ImportServer server = serverStep.getSetting();
			String gitLabProject = projectStep.getSetting().getProject();
			IssueImportOption option = optionStep.getSetting();
			logger.log("Importing issues from project " + gitLabProject + "...");
			Map<String, Optional<Long>> userIds = new HashMap<>();

			ImportResult result = server.importIssues(gitLabProject, project, option, userIds, dryRun, logger);
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Issues imported successfully")));
		});
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectStep, optionStep);
	}

}