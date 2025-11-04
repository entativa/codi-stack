package io.codibase.server.model.support.issue.field.spec.choicefield.defaultmultivalueprovider;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.server.CodiBase;
import io.codibase.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.codibase.server.util.EditContext;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.web.util.SuggestionUtils;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Editable(name="Value")
public class DefaultMultiValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> value;
	
	private String applicableProjects;

	@Editable(name="Literal value", order=100)
	@io.codibase.server.annotation.ChoiceProvider("getValueChoices")
	@NotEmpty
	@OmitName
	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}
	
	@Editable(order=200, placeholder="All projects", description="Specify applicable projects separated by space. "
			+ "Use '**', '*' or '?' for <a href='https://docs.codibase.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. "
			+ "Prefix with '-' to exclude. Leave empty for all projects")
	@Patterns(suggester="suggestProjects", path=true)
	public String getApplicableProjects() {
		return applicableProjects;
	}

	public void setApplicableProjects(String applicableProjects) {
		this.applicableProjects = applicableProjects;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestProjects(String matchWith) {
		return SuggestionUtils.suggestProjectPaths(matchWith);
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && CodiBase.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return new ArrayList<>(choiceProvider.getChoices(true).keySet());
		else
			return new ArrayList<>();
	}
	
}
