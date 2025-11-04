package io.codibase.server.plugin.pack.container;

import org.eclipse.jetty.servlet.ServletHolder;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.CodiBase;
import io.codibase.server.jetty.ServletConfigurator;
import io.codibase.server.pack.PackSupport;
import io.codibase.server.security.FilterChainConfigurator;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class ContainerModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		contribute(PackSupport.class, new ContainerPackSupport());
		bind(ContainerServlet.class);

		contribute(ServletConfigurator.class, context -> {
			context.addServlet(
					new ServletHolder(CodiBase.getInstance(ContainerServlet.class)),
					ContainerServlet.PATH + "/*");
		});

		bind(ContainerAuthenticationFilter.class);
		contribute(FilterChainConfigurator.class, filterChainManager -> {
			filterChainManager.addFilter("containerAuthc",
					CodiBase.getInstance(ContainerAuthenticationFilter.class));
			filterChainManager.createChain(
					ContainerServlet.PATH + "/**",
					"noSessionCreation, containerAuthc");
		});		
	}

}
