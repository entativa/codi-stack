package io.codibase.server.plugin.report.clover;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.buildspec.step.StepGroup;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Patterns;

@Editable(order=10000, group=StepGroup.PUBLISH, name="Jest Coverage Report")
public class PublishJestCoverageReportStep extends PublishCloverReportStep {

	private static final long serialVersionUID = 1L;
	
	@Editable(order=100, description="Specify Jest coverage report file in clover format relative to <a href='https://docs.codibase.io/concepts#job-workspace'>job workspace</a>, "
			+ "for instance <tt>coverage/clover.xml</tt>. This file can be generated with Jest option <tt>'--coverage'</tt>. "
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
