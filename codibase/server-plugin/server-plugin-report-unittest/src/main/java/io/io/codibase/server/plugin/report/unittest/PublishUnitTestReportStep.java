package io.codibase.server.plugin.report.unittest;

import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.step.PublishReportStep;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.BuildMetricService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Build;
import io.codibase.server.model.UnitTestMetric;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.persistence.dao.Dao;

import org.jspecify.annotations.Nullable;
import java.io.File;

import static io.codibase.commons.utils.LockUtils.write;
import static io.codibase.server.plugin.report.unittest.UnitTestReport.getReportLockName;

@Editable
public abstract class PublishUnitTestReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		CodiBase.getInstance(SessionService.class).run(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			File reportDir = new File(build.getDir(), UnitTestReport.CATEGORY + "/" + getReportName());

			UnitTestReport report = write(getReportLockName(build), () -> {
				UnitTestReport aReport = process(build, inputDir, logger);
				if (aReport != null) {
					FileUtils.createDir(reportDir);
					aReport.writeTo(reportDir);
					CodiBase.getInstance(ProjectService.class).directoryModified(
							build.getProject().getId(), reportDir.getParentFile());
					return aReport;
				} else {
					return null;
				}
			});

			if (report != null) {
				var metric = CodiBase.getInstance(BuildMetricService.class).find(UnitTestMetric.class, build, getReportName());
				if (metric == null) {
					metric = new UnitTestMetric();
					metric.setBuild(build);
					metric.setReportName(getReportName());
				}
				metric.setTestCaseSuccessRate(report.getTestCaseSuccessRate());
				metric.setTestSuiteSuccessRate(report.getTestSuiteSuccessRate());
				metric.setNumOfTestCases(report.getTestCases().size());
				metric.setNumOfTestSuites(report.getTestSuites().size());
				metric.setTotalTestDuration(report.getTestDuration());
				CodiBase.getInstance(Dao.class).persist(metric);
			}

		});
		return new ServerStepResult(true);
	}

	@Nullable
	protected abstract UnitTestReport process(Build build, File inputDir, TaskLogger logger);
	
}
