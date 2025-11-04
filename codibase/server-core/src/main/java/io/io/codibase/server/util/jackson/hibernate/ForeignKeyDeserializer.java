package io.codibase.server.util.jackson.hibernate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.codibase.commons.loader.AppLoader;
import io.codibase.server.model.AbstractEntity;
import io.codibase.server.persistence.dao.Dao;

import java.io.IOException;

public final class ForeignKeyDeserializer extends StdDeserializer<AbstractEntity> {

	public ForeignKeyDeserializer(Class<?> entityClass) {
		super(entityClass);
	}
	
	@Override
    public AbstractEntity getNullValue() {
        return null;
    }

    @SuppressWarnings("unchecked")
	@Override
    public AbstractEntity deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Long entityId = jp.getLongValue();
        Class<? extends AbstractEntity> valueClass = (Class<? extends AbstractEntity>) handledType();
        return (AbstractEntity) AppLoader.getInstance(Dao.class).load(valueClass, entityId);
    }
}