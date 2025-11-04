package io.codibase.server.web.component.commit.list;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.web.asset.snapsvg.SnapSvgResourceReference;
import io.codibase.server.web.page.base.BaseDependentCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class CommitListResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public CommitListResourceReference() {
		super(CommitListResourceReference.class, "commit-list.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new SnapSvgResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				CommitListResourceReference.class, "commit-list.css")));
		return dependencies;
	}

}
