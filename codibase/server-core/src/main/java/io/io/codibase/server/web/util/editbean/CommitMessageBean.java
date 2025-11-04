package io.codibase.server.web.util.editbean;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Multiline;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.ReferenceAware;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Editable
public class CommitMessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;
	
	@Editable(order=100, name="Commit Message")
	@Multiline
	@OmitName
	@NotEmpty
	@ReferenceAware
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
