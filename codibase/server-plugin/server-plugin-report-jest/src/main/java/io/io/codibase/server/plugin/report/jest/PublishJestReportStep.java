package io.codibase.server.plugin.report.jest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.CodiBase;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.model.Build;
import io.codibase.server.plugin.report.unittest.PublishUnitTestReportStep;
import io.codibase.server.plugin.report.unittest.UnitTestReport;
import io.codibase.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Patterns;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Jest Test Report")
public class PublishJestReportStep extends PublishUnitTestReportStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="Specify Jest test result file in json format relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated via Jest option <tt>'--json'</tt> and <tt>'--outputFile'</tt>. Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	@Override
	public String getFilePatterns() {
		return super.getFilePatterns();
	}

	@Override
	public void setFilePatterns(String filePatterns) {
		super.setFilePatterns(filePatterns);
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	@Override
	protected UnitTestReport process(Build build, File inputDir, TaskLogger logger) {
		ObjectMapper mapper = CodiBase.getInstance(ObjectMapper.class);

		List<TestCase> testCases = new ArrayList<>();
		int baseLen = inputDir.getAbsolutePath().length()+1;
		for (File file: FileUtils.listFiles(inputDir, Lists.newArrayList("**"), Lists.newArrayList())) {
			logger.log("Processing Jest test report: " + file.getAbsolutePath().substring(baseLen));
			try {
				testCases.addAll(JestReportParser.parse(build, mapper.readTree(file)));
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		if (!testCases.isEmpty())
			return new UnitTestReport(testCases, false);
		else
			return null;
	}

}
