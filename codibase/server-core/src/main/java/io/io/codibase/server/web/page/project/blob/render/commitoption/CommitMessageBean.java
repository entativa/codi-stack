package io.codibase.server.web.page.project.blob.render.commitoption;

import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Multiline;
import io.codibase.server.annotation.OmitName;
import io.codibase.server.annotation.ReferenceAware;
import io.codibase.server.util.ComponentContext;

import java.io.Serializable;

@Editable
public class CommitMessageBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String commitMessage;
	
	@Editable(order=100, name="Commit Message", placeholderProvider = "getDefaultCommitMessage")
	@Multiline
	@OmitName
	@ReferenceAware
	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}
	
	@SuppressWarnings("unused")
	private static String getDefaultCommitMessage() {
		return ComponentContext.get().getComponent().findParent(CommitOptionPanel.class).getDefaultCommitMessage();
	}
	
}
