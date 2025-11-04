package io.codibase.server.plugin.executor.kubernetes;

import java.util.Collection;

import org.glassfish.jersey.server.ResourceConfig;

import com.google.common.collect.Sets;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.commons.loader.ImplementationProvider;
import io.codibase.server.CodiBase;
import io.codibase.server.buildspec.job.JobExecutorDiscoverer;
import io.codibase.server.model.support.administration.jobexecutor.JobExecutor;
import io.codibase.server.rest.JerseyConfigurator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class KubernetesModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();
		
		// put your guice bindings here
		contribute(ImplementationProvider.class, new ImplementationProvider() {

			@Override
			public Class<?> getAbstractClass() {
				return JobExecutor.class;
			}

			@Override
			public Collection<Class<?>> getImplementations() {
				return Sets.newHashSet(KubernetesExecutor.class);
			}
			
		});
		
		contribute(JobExecutorDiscoverer.class, new JobExecutorDiscoverer() {
			
			@Override
			public JobExecutor discover() {
				if (CodiBase.getK8sService() != null)
					return new KubernetesExecutor();
				else
					return null;
			}

			@Override
			public int getOrder() {
				return KubernetesExecutor.ORDER;
			}
			
		});
		
		contribute(JerseyConfigurator.class, new JerseyConfigurator() {
			
			@Override
			public void configure(ResourceConfig resourceConfig) {
				resourceConfig.register(KubernetesResource.class);
			}
			
		});
		
	}

}
