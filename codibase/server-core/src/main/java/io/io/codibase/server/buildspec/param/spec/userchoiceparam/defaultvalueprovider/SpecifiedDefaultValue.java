package io.codibase.server.buildspec.param.spec.userchoiceparam.defaultvalueprovider;

import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.buildspecmodel.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.codibase.server.model.User;
import io.codibase.server.util.EditContext;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="Literal default value")
	@io.codibase.server.annotation.ChoiceProvider("getValueChoices")
	@NotEmpty
	@OmitName
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getDefaultValue() {
		return getValue();
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
