package io.codibase.server.buildspec.step.commandinterpreter;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import io.codibase.k8shelper.CommandFacade;
import io.codibase.k8shelper.RegistryLoginFacade;
import io.codibase.server.annotation.Code;
import io.codibase.server.annotation.Editable;
import io.codibase.server.annotation.Interpolative;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;

@Editable(order=100, name="Default (Shell on Linux, Batch on Windows)")
public class DefaultInterpreter extends Interpreter {

	private static final long serialVersionUID = 1L;

	@Editable(order=110, description="Specify shell commands (on Linux/Unix) or batch commands (on Windows) to execute "
			+ "under the <a href='https://docs.codibase.io/concepts#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative
	@Code(language=Code.SHELL, variableProvider="suggestVariables")
	@NotEmpty
	@Override
	public String getCommands() {
		return super.getCommands();
	}

	@Override
	public void setCommands(String commands) {
		super.setCommands(commands);
	}

	@Override
	public CommandFacade getExecutable(JobExecutor jobExecutor, String jobToken, String image,
									   String runAs, List<RegistryLoginFacade> registryLogins,
									   Map<String, String> envMap, boolean useTTY) {
		return new CommandFacade(image, runAs, registryLogins, getCommands(), envMap, useTTY);
	}
	
}
