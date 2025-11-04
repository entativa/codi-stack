package io.codibase.server.web.component.groupchoice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.Group;
import io.codibase.server.web.component.select2.Select2Choice;

public class GroupSingleChoice extends Select2Choice<Group> {

	public GroupSingleChoice(String id, IModel<Group> selectionModel, IModel<Collection<Group>> choicesModel) {
		super(id, selectionModel, new GroupChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose group..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.groupChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.groupChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.groupChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new GroupChoiceResourceReference()));
	}
	
}