package io.codibase.server.buildspec.job.gitcredential;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;

import javax.validation.constraints.NotEmpty;

import io.codibase.k8shelper.CloneInfo;
import io.codibase.k8shelper.HttpCloneInfo;
import io.codibase.server.CodiBase;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.validation.Validatable;
import io.codibase.server.annotation.ClassValidating;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;

@Editable(name="HTTP(S)", order=200)
@ClassValidating
public class HttpCredential implements GitCredential, Validatable {

	private static final long serialVersionUID = 1L;

	private String accessTokenSecret;

	@Editable(order=200, description="Specify a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as access token")
	@ChoiceProvider("getAccessTokenSecretChoices")
	@NotEmpty
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}

	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getAccessTokenSecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		return new HttpCloneInfo(CodiBase.getInstance(UrlService.class).cloneUrlFor(build.getProject(), false),
				build.getJobAuthorizationContext().getSecretValue(accessTokenSecret));
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!Project.get().getHierarchyJobSecrets().stream()
				.anyMatch(it->it.getName().equals(accessTokenSecret))) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Secret not found (" + accessTokenSecret + ")")
					.addPropertyNode("accessTokenSecret")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}

}
