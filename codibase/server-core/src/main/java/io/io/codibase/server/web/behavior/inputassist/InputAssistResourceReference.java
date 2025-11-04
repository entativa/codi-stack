package io.codibase.server.web.behavior.inputassist;

import java.util.List;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import io.codibase.server.web.asset.caret.CaretResourceReference;
import io.codibase.server.web.asset.hotkeys.HotkeysResourceReference;
import io.codibase.server.web.asset.textareacaretposition.TextareaCaretPositionResourceReference;
import io.codibase.server.web.page.base.BaseDependentCssResourceReference;
import io.codibase.server.web.page.base.BaseDependentResourceReference;

public class InputAssistResourceReference extends BaseDependentResourceReference {

	private static final long serialVersionUID = 1L;

	public InputAssistResourceReference() {
		super(InputAssistResourceReference.class, "input-assist.js");
	}

	@Override
	public List<HeaderItem> getDependencies() {
		List<HeaderItem> dependencies = super.getDependencies();
		dependencies.add(JavaScriptHeaderItem.forReference(new CaretResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new HotkeysResourceReference()));
		dependencies.add(JavaScriptHeaderItem.forReference(new TextareaCaretPositionResourceReference()));
		dependencies.add(CssHeaderItem.forReference(new BaseDependentCssResourceReference(
				InputAssistResourceReference.class, "input-assist.css")));
		return dependencies;
	}

}
