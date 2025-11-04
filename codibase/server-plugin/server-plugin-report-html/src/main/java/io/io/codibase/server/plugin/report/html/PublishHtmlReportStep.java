package io.codibase.server.plugin.report.html;

import static io.codibase.commons.utils.LockUtils.write;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.step.PublishReportStep;
import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.job.JobContext;
import io.codibase.server.job.JobService;
import io.codibase.server.model.Build;
import io.codibase.server.persistence.SessionService;

@Editable(order=1070, group= StepGroup.PUBLISH, name="Html Report")
public class PublishHtmlReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "html";
	
	public static final String START_PAGE = "$codibase-htmlreport-startpage$";

	private String startPage;

	@Editable(order=1000, description="Specify start page of the report relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>, for instance: api/index.html")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getStartPage() {
		return startPage;
	}

	public void setStartPage(String startPage) {
		this.startPage = startPage;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(SessionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			JobContext jobContext = CodiBase.getInstance(JobService.class).getJobContext(build.getId());
			if (jobContext.getJobExecutor().isHtmlReportPublishEnabled()) {
				write(getReportLockName(build), () -> {
					File reportDir = new File(build.getDir(), CATEGORY + "/" + getReportName());
					File startPage = new File(inputDir, getStartPage());
					if (startPage.exists()) {
						FileUtils.createDir(reportDir);
						File startPageFile = new File(reportDir, START_PAGE);
						FileUtils.writeFile(startPageFile, getStartPage());

						int baseLen = inputDir.getAbsolutePath().length() + 1;
						for (File file : getPatternSet().listFiles(inputDir)) {
							try {
								FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						CodiBase.getInstance(ProjectService.class).directoryModified(
								build.getProject().getId(), reportDir.getParentFile());
					} else {
						logger.warning("Html report start page not found: " + startPage.getAbsolutePath());
					}
				});
			} else {
				logger.error("Html report publish is prohibited by current job executor");
				return new ServerStepResult(false);
			}
			return new ServerStepResult(true);
		});		
	}

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}

	public static String getReportLockName(Long projectId, Long buildNumber) {
		return PublishHtmlReportStep.class.getName() + ":"	+ projectId + ":" + buildNumber;
	}
	
}
