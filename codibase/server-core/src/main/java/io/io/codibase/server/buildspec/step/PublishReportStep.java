package io.codibase.server.buildspec.step;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.k8shelper.ExecuteCondition;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.PathSegment;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.util.patternset.PatternSet;

@Editable
public abstract class PublishReportStep extends ServerSideStep {

	private static final long serialVersionUID = 1L;
	
	private String reportName;
	
	private String filePatterns;
	
	private transient PatternSet patternSet;

	@Editable(order=50, description="Specify name of the report to be displayed in build detail page")
	@PathSegment
	@NotEmpty
	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public PublishReportStep() {
		setCondition(ExecuteCondition.ALWAYS);
	}
	
	@Override
	protected PatternSet getFiles() {
		return PatternSet.parse(getFilePatterns());
	}
	
	@Editable(order=100, description="Specify files relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a> to be published. "
			+ "Use * or ? for pattern match")
	@Interpolative(variableSuggester="suggestVariables")
	@Patterns(path=true)
	@NotEmpty
	public String getFilePatterns() {
		return filePatterns;
	}

	public void setFilePatterns(String filePatterns) {
		this.filePatterns = filePatterns;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, true, false);
	}

	public PatternSet getPatternSet() {
		if (patternSet == null)
			patternSet = PatternSet.parse(getFilePatterns());
		return patternSet;
	}
	
}
