package io.codibase.server.buildspecmodel.inputspec.choiceinput;

import java.io.Serializable;
import java.util.List;

import io.codibase.server.annotation.Editable;

@Editable
public interface DefaultMultiValueProvider extends Serializable {
	
	List<String> getDefaultValue();
	
}
