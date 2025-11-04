package io.codibase.server.buildspecmodel.inputspec.booleaninput.defaultvalueprovider;

import io.codibase.server.annotation.Editable;

@Editable(order=200, name="false")
public class FalseDefaultValue implements DefaultValueProvider {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean getDefaultValue() {
		return false;
	}

}
