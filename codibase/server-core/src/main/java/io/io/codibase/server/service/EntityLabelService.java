package io.codibase.server.service;

import java.util.Collection;

import io.codibase.server.model.support.EntityLabel;
import io.codibase.server.model.support.LabelSupport;

public interface EntityLabelService<T extends EntityLabel> extends EntityService<T> {
	
	void sync(LabelSupport<T> entity, Collection<String> labelNames);
	
}
