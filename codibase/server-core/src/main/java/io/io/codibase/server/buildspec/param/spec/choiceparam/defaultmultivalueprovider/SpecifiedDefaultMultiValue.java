package io.codibase.server.buildspec.param.spec.choiceparam.defaultmultivalueprovider;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validator;

import javax.validation.constraints.NotEmpty;

import com.google.common.collect.Lists;

import io.codibase.server.CodiBase;
import io.codibase.server.buildspecmodel.inputspec.choiceinput.choiceprovider.ChoiceProvider;
import io.codibase.server.util.EditContext;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;

@Editable(order=100, name="Use specified default value")
public class SpecifiedDefaultMultiValue implements DefaultMultiValueProvider {

	private static final long serialVersionUID = 1L;

	private List<String> value;

	@Editable(name="Literal default value")
	@io.codibase.server.annotation.ChoiceProvider("getValueChoices")
	@NotEmpty
	@OmitName
	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

	@Override
	public List<String> getDefaultValue() {
		return getValue();
	}

	@SuppressWarnings("unused")
	private static List<String> getValueChoices() {
		ChoiceProvider choiceProvider = (ChoiceProvider) EditContext.get(1).getInputValue("choiceProvider");
		if (choiceProvider != null && CodiBase.getInstance(Validator.class).validate(choiceProvider).isEmpty())
			return new ArrayList<>(choiceProvider.getChoices(true).keySet());
		else
			return Lists.newArrayList();
	}

}
