package io.codibase.server.model.support;

import java.util.Collection;

public interface LabelSupport<T extends EntityLabel> {

	Collection<T> getLabels();
	
}
