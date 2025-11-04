package io.codibase.server.plugin.report.checkstyle;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Patterns;

@Editable(order=10000, group=StepGroup.PUBLISH, name="ESLint Report")
public class PublishESLintReportStep extends PublishCheckstyleReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify ESLint report file in checkstyle format under <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>. "
			+ "This file can be generated with ESLint option <tt>'-f checkstyle'</tt> and <tt>'-o'</tt>. "
			+ "Use * or ? for pattern match")
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

}
