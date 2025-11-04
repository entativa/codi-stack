package io.codibase.server.model.support.administration.jobexecutor;

import com.google.common.collect.Lists;
import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.k8shelper.RegistryLoginFacade;
import io.codibase.server.CodiBase;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.annotation.Password;
import io.codibase.server.buildspec.job.JobVariable;
import io.codibase.server.service.SettingService;
import io.codibase.server.util.interpolative.VariableInterpolator;
import io.codibase.server.web.util.SuggestionUtils;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Editable
public class RegistryLogin implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String registryUrl;
	
	private String userName;
	
	private String password;

	@Editable(order=100, placeholder="Docker Hub", description="Specify registry url. Leave empty for official registry")
	@Interpolative(variableSuggester = "suggestRegistryUrlVariables")
	public String getRegistryUrl() {
		return registryUrl;
	}
	
	public void setRegistryUrl(String registryUrl) {
		this.registryUrl = registryUrl;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestRegistryUrlVariables(String matchWith) {
		return SuggestionUtils.suggest(Lists.newArrayList(JobVariable.SERVER_URL.name().toLowerCase()), matchWith);
	}
	
	@Editable(order=200, description = "Specify user name of specified registry")
	@Interpolative(variableSuggester = "suggestUserNameVariables")
	@NotEmpty
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestUserNameVariables(String matchWith) {
		return SuggestionUtils.suggest(Lists.newArrayList(JobVariable.JOB_TOKEN.name().toLowerCase()), matchWith);
	}
	
	@Editable(order=300, name="Password", description = "Specify password or access token of specified registry")
	@NotEmpty
	@Password
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RegistryLoginFacade getFacade(String jobToken) {
		var interpolator = new VariableInterpolator(t -> {
			if (t.equalsIgnoreCase(JobVariable.SERVER_URL.name()))
				return CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl();
			else if (t.equalsIgnoreCase(JobVariable.JOB_TOKEN.name()))
				return jobToken;
			else
				throw new ExplicitException("Unrecognized interpolation variable: " + t);
		});
		return new RegistryLoginFacade(
				interpolator.interpolate(getRegistryUrl()), 
				interpolator.interpolate(getUserName()), 
				getPassword());
	}
	
}