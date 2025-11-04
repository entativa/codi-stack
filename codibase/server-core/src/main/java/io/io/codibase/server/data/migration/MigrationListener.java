package io.codibase.server.data.migration;

public interface MigrationListener {
	
	void afterMigration(Object bean);
	
}
