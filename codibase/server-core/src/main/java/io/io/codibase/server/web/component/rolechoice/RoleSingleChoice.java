package io.codibase.server.web.component.rolechoice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.Role;
import io.codibase.server.web.component.select2.Select2Choice;

public class RoleSingleChoice extends Select2Choice<Role> {

	public RoleSingleChoice(String id, IModel<Role> selectionModel, IModel<Collection<Role>> choicesModel) {
		super(id, selectionModel, new RoleChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose role..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.roleChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.roleChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.roleChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new RoleChoiceResourceReference()));
	}
	
}