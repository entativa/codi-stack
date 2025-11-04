package io.codibase.server.web.page.project.builds.detail.report;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import io.codibase.server.model.Project;
import io.codibase.server.model.support.BuildMetric;
import io.codibase.server.search.buildmetric.BuildMetricQuery;
import io.codibase.server.search.buildmetric.BuildMetricQueryParser;
import io.codibase.server.web.component.link.ViewStateAwarePageLink;
import io.codibase.server.web.page.project.stats.buildmetric.BuildMetricStatsPage;

class BuildReportTabOptionPanel extends Panel {

	private final Class<? extends BuildMetricStatsPage<?>> statsPageClass;
	
	private final String reportName;
	
	public BuildReportTabOptionPanel(String id, Class<? extends BuildMetricStatsPage<?>> statsPageClass, String reportName) {
		super(id);
		this.statsPageClass = statsPageClass;
		this.reportName = reportName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		String query = String.format("%s \"last month\" and \"%s\" %s \"%s\"", 
				BuildMetricQuery.getRuleName(BuildMetricQueryParser.Since), 
				BuildMetric.NAME_REPORT, 
				BuildMetricQuery.getRuleName(BuildMetricQueryParser.Is), 
				reportName);
		PageParameters params = BuildMetricStatsPage.paramsOf(Project.get(), query);
		add(new ViewStateAwarePageLink<>("link", statsPageClass, params));
	}

}
