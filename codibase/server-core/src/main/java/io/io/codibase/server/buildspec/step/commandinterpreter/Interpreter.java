package io.codibase.server.buildspec.step.commandinterpreter;

import io.codibase.commons.codeassist.InputSuggestion;
import io.codibase.k8shelper.CommandFacade;
import io.codibase.k8shelper.RegistryLoginFacade;
import io.codibase.server.annotation.Editable;
import io.codibase.server.buildspec.BuildSpec;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;

import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Editable
public abstract class Interpreter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String commands;
	
	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}

	public abstract CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, @Nullable String image,
												@Nullable String runAs, List<RegistryLoginFacade> registryLogins,
												Map<String, String> envMap, boolean useTTY);
	
	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, true, false, true);
	}
	
}
