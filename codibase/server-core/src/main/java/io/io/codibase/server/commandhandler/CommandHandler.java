package io.codibase.server.commandhandler;

import io.codibase.commons.bootstrap.Bootstrap;
import io.codibase.commons.loader.AbstractPlugin;
import io.codibase.commons.utils.FileUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.persistence.HibernateConfig;
import io.codibase.server.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static io.codibase.server.persistence.PersistenceUtils.callWithLock;
import static io.codibase.server.persistence.PersistenceUtils.openConnection;

public abstract class CommandHandler extends AbstractPlugin {
	
	private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

	private final HibernateConfig hibernateConfig;
	
	public CommandHandler(HibernateConfig hibernateConfig) {
		this.hibernateConfig = hibernateConfig;
	}
	
	protected <T> T doMaintenance(Callable<T> callable) {
		try {
			var maintenanceFile = CodiBase.getMaintenanceFile(Bootstrap.installDir);
			if (maintenanceFile.exists()) {
				waitForServerStop();
				return callable.call();
			} else if (!hibernateConfig.isHSQLDialect()) {
				try (var conn = openConnection(hibernateConfig, Thread.currentThread().getContextClassLoader())) {
					return callWithLock(conn, () -> {
						FileUtils.touchFile(maintenanceFile);
						try {
							waitForServerStop();
							return callable.call();
						} finally {
							FileUtils.deleteFile(maintenanceFile);
						}
					});
				}
			} else {
				FileUtils.touchFile(maintenanceFile);
				try {
					waitForServerStop();
					return callable.call();
				} finally {
					FileUtils.deleteFile(maintenanceFile);
				}
			}
		} catch (Exception e) {
			throw ExceptionUtils.unchecked(e);
		}
	}

	private void waitForServerStop() throws InterruptedException {
		logger.info("Waiting for server to stop...");
		while (CodiBase.isServerRunning(Bootstrap.installDir)) {
			Thread.sleep(1000);
		}
	}
}
