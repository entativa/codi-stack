
package	io.codibase.server.web.page.project.blob.render.source;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.web.asset.clipboard.ClipboardResourceReference;
import io.codibase.server.web.asset.codemirror.CodeMirrorResourceReference;
import io.codibase.server.web.asset.codeproblem.CodeProblemResourceReference;
import io.codibase.server.web.asset.commentindicator.CommentIndicatorCssResourceReference;
import io.codibase.server.web.asset.cookies.CookiesResourceReference;
import io.codibase.server.web.asset.hover.HoverResourceReference;
import io.codibase.server.web.asset.jqueryui.JQueryUIResourceReference;
import io.codibase.server.web.asset.selectbytyping.SelectByTypingResourceReference;
import io.codibase.server.web.asset.selectionpopover.SelectionPopoverResourceReference;
import io.codibase.server.web.page.base.BaseDependentCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class SourceViewResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public SourceViewResourceReference() {
		super(SourceViewResourceReference.class, "source-view.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new JQueryUIResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectionPopoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HoverResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new SelectByTypingResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CookiesResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeMirrorResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new ClipboardResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new CodeProblemResourceReference()));
		dependencies.add(CssHeaderItem.forReference(
				new BaseDependentCssResourceReference(SourceViewResourceReference.class, "source-view.css")));
		dependencies.add(CssHeaderItem.forReference(new CommentIndicatorCssResourceReference()));
		
		return dependencies;
	}

}
