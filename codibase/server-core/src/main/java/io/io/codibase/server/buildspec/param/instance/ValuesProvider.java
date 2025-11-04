package io.codibase.server.buildspec.param.instance;

import java.io.Serializable;
import java.util.List;

import org.jspecify.annotations.Nullable;

import io.codibase.server.buildspec.param.ParamCombination;
import io.codibase.server.model.Build;
import io.codibase.server.annotation.Editable;

@Editable
public interface ValuesProvider extends Serializable {
	
	List<List<String>> getValues(@Nullable Build build, @Nullable ParamCombination paramCombination);
	
}
