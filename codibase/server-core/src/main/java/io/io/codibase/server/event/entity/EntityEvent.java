package io.codibase.server.event.entity;

import io.codibase.server.event.Event;
import io.codibase.server.model.AbstractEntity;

public abstract class EntityEvent extends Event {
	
	private final AbstractEntity entity;
	
	public EntityEvent(AbstractEntity entity) {
		this.entity = entity;
	}

	public AbstractEntity getEntity() {
		return entity;
	}
	
}
