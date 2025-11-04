package io.codibase.server.web.component.suggestionapply;

import static io.codibase.server.web.translation.Translation._T;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.annotation.BranchChoice;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Multiline;

@Editable(name="Commit Suggestion")
public class SuggestionApplyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String branch;
	
	private String commitMessage = _T("Apply suggested change from code comment");

	@Editable(order=100, description="Specify branch to commit suggested change")
	@BranchChoice
	@NotEmpty
	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Editable(order=200)
	@Multiline
	@NotEmpty
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
}
