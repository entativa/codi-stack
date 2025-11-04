package io.codibase.server.web.page.project.tags;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.RevisionChoice;
import io.codibase.server.web.component.createtag.CreateTagBean;

import javax.validation.constraints.NotEmpty;

@Editable
public class CreateTagBeanWithRevision extends CreateTagBean {

	private static final long serialVersionUID = 1L;

	private String revision;

	@Editable(order=1000, name="Revision")
	@RevisionChoice
	@NotEmpty(message = "Please select revision to create tag from")
	@OmitName
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
	
}
