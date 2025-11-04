package io.codibase.server.plugin.report.problem;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.model.ProblemMetric;
import io.codibase.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

public class ProblemStatsPage extends BuildMetricStatsPage<ProblemMetric> {

	public ProblemStatsPage(PageParameters params) {
		super(params);
	}

	@Override
	protected Component newProjectTitle(String componentId) {
		return new Label(componentId, _T("Code Problem Statistics"));
	}

}
