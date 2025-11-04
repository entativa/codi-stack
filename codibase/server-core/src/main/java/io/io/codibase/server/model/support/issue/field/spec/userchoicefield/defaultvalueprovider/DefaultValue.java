package io.codibase.server.model.support.issue.field.spec.userchoicefield.defaultvalueprovider;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Patterns;
import io.codibase.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.codibase.server.model.User;
import io.codibase.server.util.EditContext;
import io.codibase.server.web.util.SuggestionUtils;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Editable(name="Value")
public class DefaultValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	
	private String applicableProjects;

	@Editable(order=100, name="Literal value")
	@io.codibase.server.annotation.ChoiceProvider("getValueChoices")
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
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
			return choiceProvider.getChoices(true).stream().map(User::getName).collect(Collectors.toList());
		else
			return new ArrayList<>();
	}
	
}
