package io.codibase.server.product;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.ServerConfig;
import io.codibase.server.jetty.ServerConfigurator;
import io.codibase.server.jetty.ServletConfigurator;
import io.codibase.server.persistence.HibernateConfig;
import io.codibase.server.util.ProjectNameReservation;

import java.util.HashSet;
import java.util.Set;

import static io.codibase.commons.bootstrap.Bootstrap.installDir;
import static io.codibase.server.CodiBase.getAssetsDir;

public class ProductModule extends AbstractPluginModule {

    @Override
	protected void configure() {
		super.configure();
		
		bind(HibernateConfig.class).toInstance(new HibernateConfig(installDir));
		bind(ServerConfig.class).toInstance(new ServerConfig(installDir));

		contribute(ServerConfigurator.class, ProductConfigurator.class);
		contribute(ServletConfigurator.class, ProductServletConfigurator.class);
		
		contribute(ProjectNameReservation.class, () -> {
			Set<String> reserved = new HashSet<>();
			for (var file : getAssetsDir().listFiles())
				reserved.add(file.getName());
			return reserved;
		});
		
	}

}
