package io.codibase.server.plugin.report.cppcheck;

import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import com.google.common.collect.Lists;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExceptionUtils;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TaskLogger;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.codequality.CodeProblem;
import io.codibase.server.model.Build;
import io.codibase.server.plugin.report.problem.PublishProblemReportStep;
import io.codibase.server.util.XmlUtils;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Cppcheck Report")
public class PublishCppcheckReportStep extends PublishProblemReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify cppcheck xml result file relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated with cppcheck xml output option, for instance <code>cppcheck src --xml 2>check-result.xml</code>. Use * or ? for pattern match")
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
	protected List<CodeProblem> process(Build build, File inputDir, File reportDir, TaskLogger logger) {
		SAXReader reader = new SAXReader();
		XmlUtils.disallowDocTypeDecl(reader);

		List<CodeProblem> problems = new ArrayList<>();
		int baseLen = inputDir.getAbsolutePath().length()+1;
		for (File file: FileUtils.listFiles(inputDir, Lists.newArrayList("**"), Lists.newArrayList())) {
			logger.log("Processing cppcheck report: " + file.getAbsolutePath().substring(baseLen));
			try {
				String xml = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				Document doc = reader.read(new StringReader(XmlUtils.stripDoctype(xml)));
				problems.addAll(CppcheckReportParser.parse(build, doc, logger));
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		}
		return problems;
	}

}
