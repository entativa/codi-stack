package io.codibase.server.product;

import javax.inject.Inject;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import io.codibase.server.ServerConfig;
import io.codibase.server.jetty.ServerConfigurator;

public class ProductConfigurator implements ServerConfigurator {

	private static final int REQUEST_HEADER_SIZE = 16*1024;
	
	private ServerConfig serverConfig;
	
	@Inject
	public ProductConfigurator(ServerConfig serverConfig) {
		this.serverConfig = serverConfig;
	}
	
	@Override
	public void configure(Server server) {
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(serverConfig.getHttpHost());
		connector.setPort(serverConfig.getHttpPort());
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setRequestHeaderSize(REQUEST_HEADER_SIZE);
		configuration.setSendServerVersion(false);
		connector.addConnectionFactory(new HttpConnectionFactory(configuration));
		server.addConnector(connector);
	}

}
