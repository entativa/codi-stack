package io.codibase.server.plugin.report.coverage;

import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.step.PublishReportStep;
import io.codibase.server.codequality.CoverageStatus;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.BuildMetricService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Build;
import io.codibase.server.model.CoverageMetric;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.persistence.dao.Dao;
import org.apache.commons.lang.SerializationUtils;

import org.jspecify.annotations.Nullable;
import java.io.*;
import java.util.Map;

import static io.codibase.commons.utils.LockUtils.write;
import static io.codibase.server.plugin.report.coverage.CoverageStats.getReportLockName;

@Editable
public abstract class PublishCoverageReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(SessionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			CoverageReport result = write(getReportLockName(build), () -> {
				File reportDir = new File(build.getDir(), CoverageStats.CATEGORY + "/" + getReportName());

				FileUtils.createDir(reportDir);
				try {
					CoverageReport aResult = process(build, inputDir, logger);
					if (aResult != null) {
						aResult.getStats().writeTo(reportDir);
						for (var entry: aResult.getStatuses().entrySet())
							writeLineStatuses(build, entry.getKey(), entry.getValue());

						CodiBase.getInstance(ProjectService.class).directoryModified(
								build.getProject().getId(), reportDir.getParentFile());
						return aResult;
					} else {
						FileUtils.deleteDir(reportDir);
						return null;
					}
				} catch (Exception e) {
					FileUtils.deleteDir(reportDir);
					throw ExceptionUtils.unchecked(e);
				}
			});

			if (result != null) {
				var metric = CodiBase.getInstance(BuildMetricService.class).find(CoverageMetric.class, build, getReportName());
				if (metric == null) {
					metric = new CoverageMetric();
					metric.setBuild(build);
					metric.setReportName(getReportName());
				}

				Coverage coverages = result.getStats().getOverallCoverage();
				metric.setBranchCoverage(coverages.getBranchPercentage());
				metric.setLineCoverage(coverages.getLinePercentage());

				CodiBase.getInstance(Dao.class).persist(metric);
			}
			return new ServerStepResult(true);
		});
	}

	@Nullable
	protected abstract CoverageReport process(Build build, File inputDir, TaskLogger logger);

	private void writeLineStatuses(Build build, String blobPath, Map<Integer, CoverageStatus> lineStatuses) {
		if (!lineStatuses.isEmpty()) {
			File reportDir = new File(build.getDir(), CoverageStats.CATEGORY + "/" + getReportName());
			File lineCoverageFile = new File(reportDir, CoverageStats.FILES + "/" + blobPath);
			FileUtils.createDir(lineCoverageFile.getParentFile());
			try (var os = new BufferedOutputStream(new FileOutputStream(lineCoverageFile))) {
				SerializationUtils.serialize((Serializable) lineStatuses, os);
			} catch (IOException e) {
				throw new RuntimeException(e);
			};
		}
	}
	
}
