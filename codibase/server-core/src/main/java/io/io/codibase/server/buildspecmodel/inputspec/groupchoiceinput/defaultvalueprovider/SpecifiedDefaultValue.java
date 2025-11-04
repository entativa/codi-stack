package io.codibase.server.buildspecmodel.inputspec.groupchoiceinput.defaultvalueprovider;

import java.util.List;

import javax.validation.Validator;

import javax.validation.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.codibase.server.CodiBase;
import io.codibase.server.util.EditContext;
import io.codibase.server.model.Group;
import io.codibase.server.buildspecmodel.inputspec.groupchoiceinput.choiceprovider.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.GroupChoice;
import io.codibase.server.annotation.OmitName;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	private String value;

	@Editable(name="Literal default value")
	@GroupChoice("getValueChoices")
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
	private static List<Group> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && CodiBase.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return choiceProvider.getChoices(true);
		else
			return Lists.newArrayList();
	}
	
}
