package io.codibase.server.web.page.help;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class ExampleValueChanged extends AjaxPayload {

	public ExampleValueChanged(AjaxRequestTarget target) {
		super(target);
	}

}