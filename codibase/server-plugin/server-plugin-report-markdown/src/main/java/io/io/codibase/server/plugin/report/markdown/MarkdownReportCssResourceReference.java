package io.codibase.server.plugin.report.markdown;

import io.codibase.server.web.page.base.BaseDependentCssResourceReference;

public class MarkdownReportCssResourceReference extends BaseDependentCssResourceReference {

	private static final long serialVersionUID = 1L;

	public MarkdownReportCssResourceReference() {
		super(MarkdownReportCssResourceReference.class, "markdown-report.css");
	}

}
