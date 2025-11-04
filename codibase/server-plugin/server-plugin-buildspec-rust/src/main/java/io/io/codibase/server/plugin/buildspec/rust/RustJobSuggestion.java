package io.codibase.server.plugin.buildspec.rust;

import com.google.common.collect.Lists;
import com.moandjiezana.toml.Toml;
import io.codibase.k8shelper.ExecuteCondition;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.JobSuggestion;
import io.codibase.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.codibase.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.codibase.server.buildspec.step.*;
import io.codibase.server.git.Blob;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.model.Project;
import io.codibase.server.plugin.report.clippy.PublishClippyReportStep;
import io.codibase.server.plugin.report.cobertura.PublishCoberturaReportStep;
import io.codibase.server.plugin.report.junit.PublishJUnitReportStep;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RustJobSuggestion implements JobSuggestion {

	
	private GenerateChecksumStep newChecksumGenerateStep(String name, String files) {
		var generateChecksum = new GenerateChecksumStep();
		generateChecksum.setName(name);
		generateChecksum.setFiles(files);
		generateChecksum.setTargetFile("checksum");
		return generateChecksum;
	}
	
	private Job newJob() {
		Job job = new Job();
		job.setName("rust ci");
		return job;
	}
	
	private void addCommonJobsAndTriggers(Job job) {
		var checkout = new CheckoutStep();
		checkout.setName("checkout code");
		job.getSteps().add(0, checkout);
		job.getSteps().add(newUnitTestReportPublishStep());
		job.getSteps().add(newCoverageReportPublishStep());
		job.getSteps().add(newClippyReportPublishStep());
		job.getTriggers().add(new BranchUpdateTrigger());
		job.getTriggers().add(new PullRequestUpdateTrigger());
	}

	private PublishJUnitReportStep newUnitTestReportPublishStep() {
		var publishUnitTestReport = new PublishJUnitReportStep();
		publishUnitTestReport.setName("publish unit test report");
		publishUnitTestReport.setReportName("Unit Test");
		publishUnitTestReport.setFilePatterns("target/nextest/default/test-result.xml");
		publishUnitTestReport.setCondition(ExecuteCondition.ALWAYS);
		return publishUnitTestReport;
	}

	private PublishCoberturaReportStep newCoverageReportPublishStep() {
		var publishCoverageReportStep = new PublishCoberturaReportStep();
		publishCoverageReportStep.setName("publish coverage report");
		publishCoverageReportStep.setReportName("Coverage");
		publishCoverageReportStep.setFilePatterns("coverage.xml");
		publishCoverageReportStep.setCondition(ExecuteCondition.ALWAYS);
		return publishCoverageReportStep;
	}

	private PublishClippyReportStep newClippyReportPublishStep() {
		var publishClippyReportStep = new PublishClippyReportStep();
		publishClippyReportStep.setName("publish clippy report");
		publishClippyReportStep.setReportName("Clippy");
		publishClippyReportStep.setFilePatterns("check-result.json");
		publishClippyReportStep.setCondition(ExecuteCondition.ALWAYS);
		return publishClippyReportStep;
	}
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		List<Job> jobs = new ArrayList<>();
		Blob blob;
		if ((blob = project.getBlob(new BlobIdent(commitId.name(), "Cargo.toml", FileMode.TYPE_FILE), false)) != null) {
			Job job = newJob();
			var cargoToml = new Toml().read(blob.getText().getContent());
			if (cargoToml.getString("package.version") != null) {
				CommandStep detectBuildVersion = new CommandStep();
				detectBuildVersion.setName("detect build version");
				detectBuildVersion.setImage("1dev/yq:1.0.0");
				detectBuildVersion.getInterpreter().setCommands("yq '.package.version' Cargo.toml > buildVersion");
				job.getSteps().add(detectBuildVersion);

				SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
				setBuildVersion.setName("set build version");
				setBuildVersion.setBuildVersion("@file:buildVersion@");
				job.getSteps().add(setBuildVersion);
			}
			
			job.getSteps().add(newChecksumGenerateStep("generate dependency checksum", "**/Cargo.toml **/Cargo.lock"));
			var setupCache = new SetupCacheStep();
			setupCache.setName("set up dependency cache");
			setupCache.setKey("rust_cache_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.cache/cargo"));
			setupCache.getLoadKeys().add("rust_cache");
			job.getSteps().add(setupCache);

			CommandStep buildAndTest = new CommandStep();
			buildAndTest.setName("build and test");
			
			buildAndTest.setImage("1dev/rust:1.0.3");
			buildAndTest.getInterpreter().setCommands("" +
					"set -e\n" +
					"\n" +
					"# Set CARGO_HOME to cache folder so that downloaded artifacts can be populated there. Note that we cannot cache '/root/.cargo' directly as cache folder will be mounted from an empty folder initially\n" +
					"export CARGO_HOME=/root/.cache/cargo\n" +
					"mkdir -p .config\n" +
					"cat << EOF > .config/nextest.toml\n" +
					"[profile.default.junit]\n" +
					"path = \"test-result.xml\"\n" +
					"store-success-output = true\n" +
					"store-failure-output = true\n" +
					"EOF\n" +
					"cargo llvm-cov nextest --lcov --output-path lcov.info\n" +
					"lcov_cobertura lcov.info -o coverage.xml\n" +
					"# cargo clippy --message-format=json > check-result.json\n" +
					"\n" +
					"# Make sure all files inside $CARGO_HOME is accessible by CodiBase outside of container for cache upload\n" +
					"chmod -R o+r $CARGO_HOME");
			job.getSteps().add(buildAndTest);
			
			addCommonJobsAndTriggers(job);
			jobs.add(job);
		}
		return jobs;
	}
	
}