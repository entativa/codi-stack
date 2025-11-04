package io.codibase.server.web.page.project.branches;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.RevisionChoice;
import io.codibase.server.web.component.branch.create.CreateBranchBean;

import javax.validation.constraints.NotEmpty;

@Editable
public class CreateBranchBeanWithRevision extends CreateBranchBean {

	private static final long serialVersionUID = 1L;

	private String revision;

	@Editable(order=1000)
	@RevisionChoice
	@NotEmpty(message="Please choose revision to create branch from")
	@OmitName
	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
	
}
