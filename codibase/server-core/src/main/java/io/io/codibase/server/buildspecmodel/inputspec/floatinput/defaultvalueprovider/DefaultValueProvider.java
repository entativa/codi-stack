package io.codibase.server.buildspecmodel.inputspec.floatinput.defaultvalueprovider;

import java.io.Serializable;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	float getDefaultValue();
	
}
