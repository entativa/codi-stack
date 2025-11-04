package io.codibase.server.web.component.savedquery;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class SavedQueriesOpened extends AjaxPayload {

	public SavedQueriesOpened(AjaxRequestTarget target) {
		super(target);
	}

}