package io.codibase.server.web.component.avatarupload;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class AvatarFileSelected extends AjaxPayload {

	public AvatarFileSelected(AjaxRequestTarget target) {
		super(target);
	}

}