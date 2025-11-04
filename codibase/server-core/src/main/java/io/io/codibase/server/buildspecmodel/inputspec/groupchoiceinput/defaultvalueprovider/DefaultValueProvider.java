package io.codibase.server.buildspecmodel.inputspec.groupchoiceinput.defaultvalueprovider;

import java.io.Serializable;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	String getDefaultValue();
	
}
