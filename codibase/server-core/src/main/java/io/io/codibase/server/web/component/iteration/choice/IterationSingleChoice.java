package io.codibase.server.web.component.iteration.choice;

import static io.codibase.server.web.translation.Translation._T;

import java.util.Collection;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.Iteration;
import io.codibase.server.web.component.select2.Select2Choice;

public class IterationSingleChoice extends Select2Choice<Iteration> {

	public IterationSingleChoice(String id, IModel<Iteration> selectionModel, IModel<Collection<Iteration>> iterationsModel) {
		super(id, selectionModel, new IterationChoiceProvider(iterationsModel));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose iteration..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.iterationChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.iterationChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.iterationChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IterationChoiceResourceReference()));
	}
	
}
