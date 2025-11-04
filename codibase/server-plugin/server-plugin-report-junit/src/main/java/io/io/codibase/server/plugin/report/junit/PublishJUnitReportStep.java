package io.codibase.server.plugin.report.junit;

import com.google.common.collect.Lists;
import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.model.Build;
import io.codibase.server.plugin.report.unittest.PublishUnitTestReportStep;
import io.codibase.server.plugin.report.unittest.UnitTestReport;
import io.codibase.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.codibase.server.util.XmlUtils;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Editable(order=10000, group=StepGroup.PUBLISH, name="JUnit Report")
public class PublishJUnitReportStep extends PublishUnitTestReportStep {

	private static final long serialVersionUID = 1L;

	@Editable(order=100, description="Specify JUnit test result file in XML format relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance <tt>target/surefire-reports/TEST-*.xml</tt>. Use * or ? for pattern match")
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
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);
		
		List<TestCase> testCases = new ArrayList<>();
		int baseLen = inputDir.getAbsolutePath().length()+1;
		for (File file: FileUtils.listFiles(inputDir, Lists.newArrayList("**"), Lists.newArrayList())) {
			String relativePath = file.getAbsolutePath().substring(baseLen);
			logger.log("Processing JUnit test report '" + relativePath + "'...");
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				xml = XmlUtils.stripDoctype(xml);
				testCases.addAll(JUnitReportParser.parse(reader.read(new StringReader(xml))));
			} catch (DocumentException e) {
				logger.warning("Ignored test report '" + relativePath + "' as it is not a valid XML");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (!testCases.isEmpty()) 
			return new UnitTestReport(testCases, true);
		else 
			return null;
	}

}
