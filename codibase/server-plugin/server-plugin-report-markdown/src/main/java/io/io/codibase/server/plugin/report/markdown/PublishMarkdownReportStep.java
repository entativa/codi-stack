package io.codibase.server.plugin.report.markdown;

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
import io.codibase.server.model.Build;
import io.codibase.server.persistence.SessionService;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static io.codibase.commons.utils.LockUtils.write;

@Editable(order=1100, group=StepGroup.PUBLISH, name="Markdown Report")
public class PublishMarkdownReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	public static final String CATEGORY = "markdown";
	
	public static final String START_PAGE = "$codibase-startpage$";
	
	private String startPage;
	
	@Editable(order=1100, description="Specify start page of the report relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>, for instance: <tt>manual/index.md</tt>")
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

	public static String getReportLockName(Build build) {
		return getReportLockName(build.getProject().getId(), build.getNumber());
	}
	
	public static String getReportLockName(Long projectId, Long buildNumber) {
		return PublishMarkdownReportStep.class.getName() + ":" + projectId + ":" + buildNumber;
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		CodiBase.getInstance(SessionService.class).run(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			write(getReportLockName(build), () -> {
				File startPage = new File(inputDir, getStartPage());
				if (startPage.exists()) {
					File reportDir = new File(build.getDir(), CATEGORY + "/" + getReportName());

					FileUtils.createDir(reportDir);
					File startPageFile = new File(reportDir, START_PAGE);
					FileUtils.writeFile(startPageFile, getStartPage());

					int baseLen = inputDir.getAbsolutePath().length() + 1;
					for (File file: getPatternSet().listFiles(inputDir)) {
						try {
							FileUtils.copyFile(file, new File(reportDir, file.getAbsolutePath().substring(baseLen)));
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					CodiBase.getInstance(ProjectService.class).directoryModified(
							build.getProject().getId(), reportDir.getParentFile());
				} else {
					logger.warning("Markdown report start page not found: " + startPage.getAbsolutePath());
				}
				return null;
			});
		});
		return new ServerStepResult(true);
	}

}
