package io.codibase.server.plugin.buildspec.dotnet;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.buildspec.job.JobSuggestion;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class DotnetModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, DotnetJobSuggestion.class);
	}

}
