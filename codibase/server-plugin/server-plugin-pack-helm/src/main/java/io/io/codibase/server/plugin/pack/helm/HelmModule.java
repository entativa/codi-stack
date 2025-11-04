package io.codibase.server.plugin.pack.helm;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.pack.PackHandler;
import io.codibase.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class HelmModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(HelmPackHandler.class);
		contribute(PackHandler.class, HelmPackHandler.class);
		contribute(PackSupport.class, new HelmPackSupport());
	}

}
