package io.codibase.server.web.component.stringchoice;

import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import io.codibase.server.web.component.select2.Select2Choice;

public class StringSingleChoice extends Select2Choice<String> {

	public StringSingleChoice(String id, IModel<String> selectionModel, 
			IModel<List<String>> choicesModel, 
			IModel<Map<String, String>> displayNamesModel, 
			IModel<Map<String, String>> descriptionsModel,
			boolean tagsMode) {
		super(id, selectionModel, new StringChoiceProvider(choicesModel, displayNamesModel, descriptionsModel, tagsMode));
	}

	public StringSingleChoice(String id, IModel<String> selectionModel, 
			IModel<List<String>> choicesModel, 
			IModel<Map<String, String>> displayNamesModel,
			boolean tagsMode) {
		super(id, selectionModel, new StringChoiceProvider(choicesModel, displayNamesModel, tagsMode));
	}

	public StringSingleChoice(String id, IModel<String> selectionModel, IModel<List<String>> choicesModel, boolean tagsMode) {
		super(id, selectionModel, new StringChoiceProvider(choicesModel, tagsMode));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setAllowClear(!isRequired());
		getSettings().setFormatResult("codibase.server.choiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.choiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.choiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

}
