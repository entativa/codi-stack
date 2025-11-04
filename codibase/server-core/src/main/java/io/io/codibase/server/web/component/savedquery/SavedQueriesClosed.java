package io.codibase.server.web.component.savedquery;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class SavedQueriesClosed extends AjaxPayload {

	public SavedQueriesClosed(AjaxRequestTarget target) {
		super(target);
	}

}