package io.codibase.server.buildspec.param.instance;

import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.param.ParamCombination;
import io.codibase.server.model.Build;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;

@Editable
public interface ValueProvider extends Serializable {
	
	List<String> getValue(@Nullable Build build, @Nullable ParamCombination paramCombination);
	
}
