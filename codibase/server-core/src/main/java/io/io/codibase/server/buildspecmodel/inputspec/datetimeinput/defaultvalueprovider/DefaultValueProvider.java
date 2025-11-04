package io.codibase.server.buildspecmodel.inputspec.datetimeinput.defaultvalueprovider;

import java.io.Serializable;
import java.util.Date;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultValueProvider extends Serializable {
	
	Date getDefaultValue();
	
}
