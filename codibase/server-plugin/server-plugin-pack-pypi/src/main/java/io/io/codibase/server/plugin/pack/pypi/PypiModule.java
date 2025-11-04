package io.codibase.server.plugin.pack.pypi;

import io.codibase.commons.loader.AbstractPluginModule;
import io.codibase.server.pack.PackHandler;
import io.codibase.server.pack.PackSupport;

/**
 * NOTE: Do not forget to rename moduleClass property defined in the pom if you've renamed this class.
 *
 */
public class PypiModule extends AbstractPluginModule {

	@Override
	protected void configure() {
		super.configure();

		bind(PypiPackHandler.class);
		contribute(PackHandler.class, PypiPackHandler.class);
		contribute(PackSupport.class, new PypiPackSupport());
	}

}
