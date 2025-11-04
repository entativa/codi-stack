package io.codibase.server.buildspec.job;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.annotation.EnvironmentName;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;

@Editable
public class EnvVar implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String value;

	@Editable(order=100, description="Specify name of the environment variable")
	@Interpolative(variableSuggester="suggestVariables")
	@EnvironmentName
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Editable(order=200, description="Specify value of the environment variable")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@SuppressWarnings("unused")
	private static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}

}
