package io.codibase.server.plugin.imports.youtrack;

import com.google.common.collect.Lists;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.imports.IssueImporter;
import io.codibase.server.web.component.taskbutton.TaskResult;
import io.codibase.server.web.util.ImportStep;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;

public class YouTrackIssueImporter implements IssueImporter {

	private static final long serialVersionUID = 1L;
	
	private final ImportStep<ImportServer> serverStep = new ImportStep<ImportServer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Authenticate to YouTrack");
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
	
	private final ImportStep<ImportOption> optionStep = new ImportStep<ImportOption>() {

		private static final long serialVersionUID = 1L;

		@Override
		public String getTitle() {
			return _T("Specify import option");
		}

		@Override
		protected ImportOption newSetting() {
			ImportServer server = serverStep.getSetting();
			String project = projectStep.getSetting().getProject();
			return server.buildImportOption(project, projectStep.getSetting().isPopulateTagMappings());
		}
		
	};
	
	@Override
	public String getName() {
		return YouTrackModule.NAME;
	}

	@Override
	public TaskResult doImport(Long projectId, boolean dryRun, TaskLogger logger) {
		ImportServer server = serverStep.getSetting();
		String youTrackProject = projectStep.getSetting().getProject();
		ImportOption option = optionStep.getSetting();
		return server.importIssues(projectId, youTrackProject, option, dryRun, logger);
	}

	@Override
	public List<ImportStep<? extends Serializable>> getSteps() {
		return Lists.newArrayList(serverStep, projectStep, optionStep);
	}

}