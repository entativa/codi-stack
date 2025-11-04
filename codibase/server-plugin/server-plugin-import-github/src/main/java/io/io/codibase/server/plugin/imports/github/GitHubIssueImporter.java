package io.codibase.server.plugin.imports.github;

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

public class GitHubIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Authenticate to GitHub");
		}

		@Override
		protected ImportServer newSetting() {
			return new ImportServer();
		}
		
	};
	
	private final ImportStep<ImportRepository> repositoryStep = new ImportStep<ImportRepository>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Choose repository");
		}

		@Override
		protected ImportRepository newSetting() {
			ImportRepository repository = new ImportRepository();
			repository.server = serverStep.getSetting();
			return repository;
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
					Lists.newArrayList(repositoryStep.getSetting().getRepository()));
		}
		
	};
	
	@Override
	public String getName() {
		return GitHubModule.NAME;
	}
	
	@Override
	public TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger) {
		return CodiBase.getInstance(TransactionService.class).call(() -> {
			var project = CodiBase.getInstance(ProjectService.class).load(projectId);
			logger.log("Importing issues from repository " + repositoryStep.getSetting().getRepository() + "...");
			Map<String, Optional<Long>> userIds = new HashMap<>();

			ImportResult result = serverStep.getSetting().importIssues(repositoryStep.getSetting().getRepository(),
					project, optionStep.getSetting(), userIds, dryRun, logger);
			return new TaskResult(true, new HtmlMessgae(result.toHtml("Issues imported successfully")));
		});
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, repositoryStep, optionStep);
	}

}