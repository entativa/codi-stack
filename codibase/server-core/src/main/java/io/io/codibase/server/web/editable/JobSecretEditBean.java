package io.codibase.server.web.editable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.server.model.support.build.JobSecret;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;
import io.codibase.server.web.page.project.blob.ProjectBlobPage;
import io.codibase.server.web.util.WicketUtils;

@Editable
public class JobSecretEditBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private String secret;

	@Editable
	@ChoiceProvider("getSecretChoices")
	@NotEmpty
	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getSecretChoices() {
		List<String> secretNames = new ArrayList<>();
		ProjectBlobPage page = (ProjectBlobPage) WicketUtils.getPage();
		for (JobSecret secret: page.getProject().getHierarchyJobSecrets()) 
			secretNames.add(secret.getName());
		return secretNames;
	}
	
}
