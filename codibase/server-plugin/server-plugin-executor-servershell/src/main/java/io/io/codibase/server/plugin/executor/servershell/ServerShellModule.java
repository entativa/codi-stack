package io.codibase.server.plugin.executor.servershell;

import java.util.Collection;

import com.google.common.collect.Sets;

import io.codibase.commons.bootstrap.Bootstrap;
import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.commons.loader.ImplementationProvider;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ServerShellModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		if (!Bootstrap.isInDocker()) {
			contribute(ImplementationProvider.class, new ImplementationProvider() {

				@Override
				public Class<?> getAbstractClass() {
					return JobExecutor.class;
				}

				@Override
				public Collection<Class<?>> getImplementations() {
					return Sets.newHashSet(ServerShellExecutor.class);
				}
				
			});						
		}
	}

}
