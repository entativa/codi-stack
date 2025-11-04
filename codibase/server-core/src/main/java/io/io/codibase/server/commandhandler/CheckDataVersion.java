package io.codibase.server.commandhandler;

import static io.codibase.server.persistence.PersistenceUtils.callWithTransaction;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.server.data.DataService;
import io.codibase.server.persistence.HibernateConfig;
import io.codibase.server.persistence.SessionFactoryService;
import io.codibase.server.security.SecurityUtils;

@Singleton
public class CheckDataVersion extends CommandHandler {

	public static final String COMMAND = "check-data-version";
	
	private static final Logger logger = LoggerFactory.getLogger(CheckDataVersion.class);

	private final SessionFactoryService sessionFactoryService;
	
	private final DataService dataService;
		
	@Inject
	public CheckDataVersion(SessionFactoryService sessionFactoryService, DataService dataService,
                            HibernateConfig hibernateConfig) {
		super(hibernateConfig);
		this.sessionFactoryService = sessionFactoryService;
		this.dataService = dataService;
	}

	@Override
	public void start() {
		SecurityUtils.bindAsSystem();

		try {
			doMaintenance(() -> {
				sessionFactoryService.start();

				// Use system.out in case logger is suppressed by user as this output is important to 
				// upgrade procedure
				String dataVersion;
				try (var conn = dataService.openConnection()) {
					dataVersion = callWithTransaction(conn, () -> dataService.checkDataVersion(conn, false));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
				System.out.println("Data version: " + dataVersion);
				return null;
			});
			System.exit(0);
		} catch (ExplicitException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}

	@Override
	public void stop() {
		sessionFactoryService.stop();
	}

}
