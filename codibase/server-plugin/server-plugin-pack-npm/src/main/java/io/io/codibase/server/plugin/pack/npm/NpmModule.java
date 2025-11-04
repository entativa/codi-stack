package io.codibase.server.plugin.pack.npm;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.pack.PackHandler;
import io.codibase.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class NpmModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(NpmPackHandler.class);
		contribute(PackHandler.class, NpmPackHandler.class);
		contribute(PackSupport.class, new NpmPackSupport());
	}

}
