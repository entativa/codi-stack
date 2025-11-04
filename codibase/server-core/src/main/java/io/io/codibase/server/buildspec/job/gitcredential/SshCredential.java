package io.codibase.server.buildspec.job.gitcredential;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidatorContext;

import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import javax.validation.constraints.NotEmpty;

import io.codibase.k8shelper.CloneInfo;
import io.codibase.k8shelper.SshCloneInfo;
import io.codibase.server.CodiBase;
import io.codibase.server.service.SettingService;
import io.codibase.server.web.UrlService;
import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.model.support.administration.SshSetting;
import io.codibase.server.model.support.administration.SystemSetting;
import io.codibase.server.validation.Validatable;
import io.codibase.server.annotation.ClassValidating;
import io.codibase.server.annotation.ChoiceProvider;
import io.codibase.server.annotation.Editable;

@Editable(name="SSH", order=300)
@ClassValidating
public class SshCredential implements GitCredential, Validatable {

	private static final long serialVersionUID = 1L;

	private String keySecret;

	@Editable(order=100, description="Specify a <a href='https://docs.codibase.io/tutorials/cicd/job-secrets' target='_blank'>job secret</a> to be used as SSH private key")
	@ChoiceProvider("getKeySecretChoices")
	@NotEmpty
	public String getKeySecret() {
		return keySecret;
	}

	public void setKeySecret(String keySecret) {
		this.keySecret = keySecret;
	}
	
	@SuppressWarnings("unused")
	private static List<String> getKeySecretChoices() {
		return Project.get().getHierarchyJobSecrets()
				.stream().map(it->it.getName()).collect(Collectors.toList());
	}

	@Override
	public CloneInfo newCloneInfo(Build build, String jobToken) {
		String cloneUrl = CodiBase.getInstance(UrlService.class).cloneUrlFor(build.getProject(), true);
		SettingService settingService = CodiBase.getInstance(SettingService.class);
		SystemSetting systemSetting = settingService.getSystemSetting();
		SshSetting sshSetting = settingService.getSshSetting();
		StringBuilder knownHosts = new StringBuilder(systemSetting.getSshServerName()).append(" ");
		try {
			PublicKeyEntry.appendPublicKeyEntry(knownHosts, 
					KeyUtils.recoverPublicKey(sshSetting.getPrivateKey()));
		} catch (IOException|GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
		
		knownHosts.append("\n");
		
		String privateKey = build.getJobAuthorizationContext().getSecretValue(keySecret);
		if (!privateKey.endsWith("\n"))
			privateKey = privateKey + "\n";
		
		return new SshCloneInfo(cloneUrl, privateKey, knownHosts.toString());
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		if (!Project.get().getHierarchyJobSecrets().stream()
				.anyMatch(it->it.getName().equals(keySecret))) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("Secret not found (" + keySecret + ")")
					.addPropertyNode("keySecret")
					.addConstraintViolation();
			return false;
		} else {
			return true;
		}
	}
	
}
