package io.codibase.server.plugin.buildspec.node;

import com.google.common.collect.Lists;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.buildspec.job.JobSuggestion;
import io.codibase.server.model.support.administration.GroovyScript;
import io.codibase.server.util.ScriptContribution;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class NodePluginModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(JobSuggestion.class, NodeJobSuggestion.class);
		
		contribute(ScriptContribution.class, new ScriptContribution() {

			@Override
			public GroovyScript getScript() {
				GroovyScript script = new GroovyScript();
				script.setName(NodeJobSuggestion.DETERMINE_PROJECT_VERSION);
				script.setContent(Lists.newArrayList("io.codibase.server.plugin.buildspec.node.NodeJobSuggestion.determineProjectVersion()"));
				return script;
			}
			 
		});
		
	}

}
