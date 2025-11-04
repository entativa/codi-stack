package io.codibase.server.web.component.issue.choice;

import static io.codibase.server.web.translation.Translation._T;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;

import io.codibase.server.model.Issue;
import io.codibase.server.web.component.select2.Select2Choice;

public class IssueSingleChoice extends Select2Choice<Issue> {

	public IssueSingleChoice(String id, IModel<Issue> model, IssueChoiceProvider choiceProvider) {
		super(id, model, choiceProvider);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		getSettings().setAllowClear(!isRequired());
		if (isRequired())
			getSettings().setPlaceholder(_T("Choose issue..."));
		else
			getSettings().setPlaceholder(_T("Not specified"));
		getSettings().setFormatResult("codibase.server.issueChoiceFormatter.formatResult");
		getSettings().setFormatSelection("codibase.server.issueChoiceFormatter.formatSelection");
		getSettings().setEscapeMarkup("codibase.server.issueChoiceFormatter.escapeMarkup");
		setConvertEmptyInputStringToNull(true);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new IssueChoiceResourceReference()));
	}
	
}
