package io.codibase.server.web.component.user.choice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.User;
import io.codibase.server.web.component.select2.Select2MultiChoice;

public class UserMultiChoice extends Select2MultiChoice<User> {

	private static final long serialVersionUID = 1L;

	public UserMultiChoice(String id, IModel<Collection<User>> selectionsModel, 
			IModel<List<User>> choicesModel) {
		super(id, selectionsModel, new UserChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose users..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.userChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.userChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.userChoiceFormatter.escapeMarkup");
        setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new UserChoiceResourceReference()));
	}

}
