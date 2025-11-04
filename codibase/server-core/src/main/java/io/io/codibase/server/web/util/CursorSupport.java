package io.codibase.server.web.util;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.model.AbstractEntity;

public interface CursorSupport<T extends AbstractEntity> extends Serializable {
	
	Cursor getCursor();
	
	void navTo(AjaxRequestTarget target, T entity, Cursor cursor);
	
}
