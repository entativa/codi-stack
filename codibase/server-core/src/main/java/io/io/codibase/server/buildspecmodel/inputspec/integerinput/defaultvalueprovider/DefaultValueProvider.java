package io.codibase.server.buildspecmodel.inputspec.integerinput.defaultvalueprovider;

import java.io.Serializable;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	int getDefaultValue();
	
}
