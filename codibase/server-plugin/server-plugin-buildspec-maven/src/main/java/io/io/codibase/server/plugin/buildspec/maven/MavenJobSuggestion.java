package io.codibase.server.plugin.buildspec.maven;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;

import com.google.common.collect.Lists;

import io.codibase.k8shelper.ExecuteCondition;
import io.codibase.server.buildspec.job.Job;
import io.codibase.server.buildspec.job.JobSuggestion;
import io.codibase.server.buildspec.job.trigger.BranchUpdateTrigger;
import io.codibase.server.buildspec.job.trigger.PullRequestUpdateTrigger;
import io.codibase.server.buildspec.step.CheckoutStep;
import io.codibase.server.buildspec.step.CommandStep;
import io.codibase.server.buildspec.step.GenerateChecksumStep;
import io.codibase.server.buildspec.step.SetBuildVersionStep;
import io.codibase.server.buildspec.step.SetupCacheStep;
import io.codibase.server.git.Blob;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.model.Project;
import io.codibase.server.plugin.report.junit.PublishJUnitReportStep;

public class MavenJobSuggestion implements JobSuggestion {
	
	@Override
	public Collection<Job> suggestJobs(Project project, ObjectId commitId) {
		Collection<Job> jobs = new ArrayList<>();
		
		Blob blob = project.getBlob(new BlobIdent(commitId.name(), "pom.xml", FileMode.TYPE_FILE), false);
		if (blob != null) {
			Job job = new Job();
			job.setName("maven ci");

			CheckoutStep checkout = new CheckoutStep();
			checkout.setName("checkout code");
			job.getSteps().add(checkout);
			
			var generateChecksum = new GenerateChecksumStep();
			generateChecksum.setName("generate pom checksum");
			generateChecksum.setFiles("**/pom.xml");
			generateChecksum.setTargetFile("checksum");
			job.getSteps().add(generateChecksum);

			var setupCache = new SetupCacheStep();
			setupCache.setName("set up maven cache");
			setupCache.setKey("maven_repository_@file:checksum@");
			setupCache.setPaths(Lists.newArrayList("/root/.m2/repository"));
			setupCache.getLoadKeys().add("maven_repository");
			job.getSteps().add(setupCache);
			
			CommandStep runTests = new CommandStep();
			runTests.setName("run tests");
			runTests.setImage("maven");
			runTests.getInterpreter().setCommands("mvn clean test\n");
			job.getSteps().add(runTests);

			CommandStep detectBuildVersion = new CommandStep();
			detectBuildVersion.setName("detect build version");
			detectBuildVersion.setImage("maven");
			detectBuildVersion.getInterpreter().setCommands("" +
					"echo \"Detecting project version (may require some time while downloading maven dependencies)...\"\n" +
					"mvn org.apache.maven.plugins:maven-help-plugin:3.1.0:evaluate -Dexpression=project.version -q -DforceStdout > buildVersion || { cat buildVersion; exit 1; }");
			job.getSteps().add(detectBuildVersion);

			SetBuildVersionStep setBuildVersion = new SetBuildVersionStep();
			setBuildVersion.setName("set build version");
			setBuildVersion.setBuildVersion("@file:buildVersion@");
			job.getSteps().add(setBuildVersion);
			
			var publishUnitTestReportStep = new PublishJUnitReportStep();
			publishUnitTestReportStep.setName("publish unit test report");
			publishUnitTestReportStep.setReportName("Unit Test");
			publishUnitTestReportStep.setFilePatterns("**/TEST-*.xml");
			publishUnitTestReportStep.setCondition(ExecuteCondition.ALWAYS);
			job.getSteps().add(publishUnitTestReportStep);
			
			job.getTriggers().add(new BranchUpdateTrigger());
			job.getTriggers().add(new PullRequestUpdateTrigger());
			
			jobs.add(job);
		} 
		return jobs;
	}
	
}