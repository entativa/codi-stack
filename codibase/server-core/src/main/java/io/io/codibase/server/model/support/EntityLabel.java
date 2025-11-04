package io.codibase.server.model.support;

import javax.persistence.MappedSuperclass;

import io.codibase.server.model.AbstractEntity;
import io.codibase.server.model.LabelSpec;

@MappedSuperclass
public abstract class EntityLabel extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	public abstract AbstractEntity getEntity();
	
	public abstract LabelSpec getSpec();

}
