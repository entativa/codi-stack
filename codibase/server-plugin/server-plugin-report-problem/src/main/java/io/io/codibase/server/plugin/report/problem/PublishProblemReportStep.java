package io.codibase.server.plugin.report.problem;

import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.LockUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.k8shelper.ServerStepResult;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.step.PublishReportStep;
import io.codibase.server.codequality.CodeProblem;
import io.codibase.server.codequality.CodeProblem.Severity;
import io.codibase.server.codequality.BlobTarget;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.BuildMetricService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Build;
import io.codibase.server.model.ProblemMetric;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.persistence.dao.Dao;
import org.apache.commons.lang3.SerializationUtils;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.Collection;
import java.util.List;

import static io.codibase.server.codequality.CodeProblem.Severity.*;

@Editable
public abstract class PublishProblemReportStep extends PublishReportStep {

	private static final long serialVersionUID = 1L;

	private Severity failThreshold = HIGH;
	
	@Editable(order=1000, name="Fail Threshold", description = "Fail build if there are vulnerabilities " +
			"with or severer than specified severity level. Note that this only takes effect if build " +
			"is not failed by other steps")
	@NotNull
	public Severity getFailThreshold() {
		return failThreshold;
	}

	public void setFailThreshold(Severity failThreshold) {
		this.failThreshold = failThreshold;
	}
	
	@Override
	public ServerStepResult run(Long buildId, File inputDir, TaskLogger logger) {
		return CodiBase.getInstance(SessionService.class).call(() -> {
			var build = CodiBase.getInstance(BuildService.class).load(buildId);
			File reportDir = new File(build.getDir(), ProblemReport.CATEGORY + "/" + getReportName());

			ProblemReport report = LockUtils.write(ProblemReport.getReportLockName(build), () -> {
				FileUtils.createDir(reportDir);
				try {
					var problems = process(build, inputDir, reportDir, logger);
					if (!problems.isEmpty()) {
						var aReport = new ProblemReport(problems);
						aReport.writeTo(reportDir);
						for (var group: aReport.getProblemGroups()) {
							if (group.getKey() instanceof BlobTarget.GroupKey)
								writeFileProblems(build, group.getKey().getName(), group.getProblems());
						}
						CodiBase.getInstance(ProjectService.class).directoryModified(
								build.getProject().getId(), reportDir.getParentFile());
						return aReport;
					} else {
						FileUtils.deleteDir(reportDir);
						return null;
					}
				} catch (Exception e) {
					FileUtils.deleteDir(reportDir);
					throw ExceptionUtils.unchecked(e);
				}
			});

			if (report != null) {
				FileUtils.createDir(reportDir);
				report.writeTo(reportDir);

				var metric = CodiBase.getInstance(BuildMetricService.class).find(ProblemMetric.class, build, getReportName());
				if (metric == null) {
					metric = new ProblemMetric();
					metric.setBuild(build);
					metric.setReportName(getReportName());
				}
				metric.setCriticalSeverities((int) report.getProblems().stream()
						.filter(it-> it.getSeverity() == CRITICAL)
						.count());
				metric.setHighSeverities((int) report.getProblems().stream()
						.filter(it-> it.getSeverity() == HIGH)
						.count());
				metric.setMediumSeverities((int) report.getProblems().stream()
						.filter(it-> it.getSeverity() == MEDIUM)
						.count());
				metric.setLowSeverities((int) report.getProblems().stream()
						.filter(it-> it.getSeverity() == LOW)
						.count());
				CodiBase.getInstance(Dao.class).persist(metric);

				if (report.getProblems().stream().anyMatch(it -> it.getSeverity().ordinal() <= failThreshold.ordinal())) {
					logger.error(getReportName() + ": found problems with or severer than " + failThreshold + " severity");
					return new ServerStepResult(false);
				} else {
					return new ServerStepResult(true);
				}
			} else {
				return new ServerStepResult(true);
			}
		});
	}
	
	private void writeFileProblems(Build build, String blobPath, Collection<CodeProblem> problemsOfFile) {
		File reportDir = new File(build.getDir(), ProblemReport.CATEGORY + "/" + getReportName());
		File violationsFile = new File(reportDir, ProblemReport.FILES + "/" + blobPath);
		FileUtils.createDir(violationsFile.getParentFile());
		try (var os = new BufferedOutputStream(new FileOutputStream(violationsFile))) {
			SerializationUtils.serialize((Serializable) problemsOfFile, os);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract List<CodeProblem> process(Build build, File inputDir, File reportDir, TaskLogger logger);
	
}
