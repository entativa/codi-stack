package io.codibase.server.plugin.report.ruff;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.commons.loader.ImplementationProvider;
import io.codibase.server.buildspec.step.PublishReportStep;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class RuffModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return PublishReportStep.class;
			}
			
			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(PublishRuffReportStep.class);
			}
			
		});
		
	}

}
