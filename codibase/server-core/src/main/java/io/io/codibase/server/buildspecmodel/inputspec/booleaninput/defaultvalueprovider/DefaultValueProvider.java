package io.codibase.server.buildspecmodel.inputspec.booleaninput.defaultvalueprovider;

import java.io.Serializable;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	boolean getDefaultValue();
	
}
