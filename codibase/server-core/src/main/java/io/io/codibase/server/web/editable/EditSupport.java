package io.codibase.server.web.editable;

import java.io.Serializable;

import io.codibase.commons.loader.ExtensionPoint;

import org.jspecify.annotations.Nullable;

@ExtensionPoint
public interface EditSupport extends Serializable {
	
	int DEFAULT_PRIORITY = 100;
	
	@Nullable
	PropertyContext<?> getEditContext(PropertyDescriptor descriptor);
	
	int getPriority();
	
}
