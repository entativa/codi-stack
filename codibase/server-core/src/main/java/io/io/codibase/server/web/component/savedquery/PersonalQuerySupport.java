package io.codibase.server.web.component.savedquery;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public interface PersonalQuerySupport extends Serializable {

	void onSave(AjaxRequestTarget target, String queryName);

}
