package io.codibase.server.event.entity;

import io.codibase.server.model.AbstractEntity;

public class EntityRemoved extends EntityEvent {
	
	public EntityRemoved(AbstractEntity entity) {
		super(entity);
	}

}
