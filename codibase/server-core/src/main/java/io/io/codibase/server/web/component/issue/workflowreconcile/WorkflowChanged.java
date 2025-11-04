package io.codibase.server.web.component.issue.workflowreconcile;

import org.apache.wicket.ajax.AjaxRequestTarget;

import io.codibase.server.web.util.AjaxPayload;

public class WorkflowChanged extends AjaxPayload {

	public WorkflowChanged(AjaxRequestTarget target) {
		super(target);
	}

}