package io.codibase.server.web.component.sideinfo;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class SideInfoOpened extends AjaxPayload {

	public SideInfoOpened(AjaxRequestTarget target) {
		super(target);
	}

}