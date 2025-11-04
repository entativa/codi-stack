package io.codibase.server.web.component.project.choice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.List;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.Project;
import io.codibase.server.web.component.select2.Select2Choice;

public class ProjectSingleChoice extends Select2Choice<Project> {

	public ProjectSingleChoice(String id, IModel<Project> model, IModel<List<Project>> choicesModel) {
		super(id, model, new ProjectChoiceProvider(choicesModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose a project..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.projectChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.projectChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.projectChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new ProjectChoiceResourceReference()));
	}
	
}