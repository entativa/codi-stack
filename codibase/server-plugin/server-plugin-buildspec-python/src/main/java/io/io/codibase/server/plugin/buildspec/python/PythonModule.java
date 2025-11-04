package io.codibase.server.plugin.buildspec.python;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.buildspec.job.JobSuggestion;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class PythonModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, PythonJobSuggestion.class);
	}

}
