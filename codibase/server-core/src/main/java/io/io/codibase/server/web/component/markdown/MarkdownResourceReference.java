package io.codibase.server.web.component.markdown;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.web.asset.atwho.AtWhoResourceReference;
import io.codibase.server.web.asset.caret.CaretResourceReference;
import io.codibase.server.web.asset.clipboard.ClipboardResourceReference;
import io.codibase.server.web.asset.cookies.CookiesResourceReference;
import io.codibase.server.web.asset.diff.DiffResourceReference;
import io.codibase.server.web.asset.doneevents.DoneEventsResourceReference;
import io.codibase.server.web.asset.hotkeys.HotkeysResourceReference;
import io.codibase.server.web.asset.hover.HoverResourceReference;
import io.codibase.server.web.asset.jqueryui.JQueryUIResourceReference;
import io.codibase.server.web.asset.textareacaretposition.TextareaCaretPositionResourceReference;
import io.codibase.server.web.component.commit.status.CommitStatusCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class MarkdownResourceReference extends BaseDependentResourceReference {

	public MarkdownResourceReference() {
		super(MarkdownResourceReference.class, "markdown.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new AtWhoResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DiffResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TextareaCaretPositionResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new DoneEventsResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		
		dependencies.add(CssHeaderItem.forReference(new CommitStatusCssResourceReference()));

		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(MarkdownResourceReference.class, "markdown.css")));
		return dependencies;
	}
	
}
