package io.codibase.server.web.component.stringchoice;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import io.codibase.server.web.component.select2.Select2MultiChoice;

public class StringMultiChoice extends Select2MultiChoice<String> {

	public StringMultiChoice(
			String id, 
			IModel<Collection<String>> selectionsModel, 
			IModel<List<String>> choicesModel, 
			IModel<Map<String, String>> displayNamesModel, 
			IModel<Map<String, String>> descriptionsModel,
			boolean tagsMode) {
		super(id, selectionsModel, new StringChoiceProvider(choicesModel, displayNamesModel, descriptionsModel, tagsMode));
	}

	public StringMultiChoice(
			String id, 
			IModel<Collection<String>> selectionsModel, 
			IModel<List<String>> choicesModel, 
			IModel<Map<String, String>> displayNamesModel, 
			boolean tagsMode) {
		super(id, selectionsModel, new StringChoiceProvider(choicesModel, displayNamesModel, tagsMode));
	}

	public StringMultiChoice(
			String id, 
			IModel<Collection<String>> selectionsModel, 
			IModel<List<String>> choicesModel,
			boolean tagsMode) {
		super(id, selectionsModel, new StringChoiceProvider(choicesModel, tagsMode));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setFormatResult("codibase.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
