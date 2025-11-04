package io.codibase.server.git.command;

import io.codibase.commons.utils.command.Commandline;
import io.codibase.commons.utils.command.LineConsumer;
import io.codibase.server.git.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LfsFetchAllCommand {

	private static final Logger logger = LoggerFactory.getLogger(LfsFetchAllCommand.class);
	
	private final File workingDir;
	
	private final String remoteUrl;
	
	public LfsFetchAllCommand(File workingDir, String remoteUrl) {
		this.workingDir = workingDir;
		this.remoteUrl = remoteUrl;
	}
	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public void run() {
		Commandline git = newGit().workingDir(workingDir);

		git.addArgs("lfs", "fetch", remoteUrl, "--all");
		git.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.trace(line);
			}

		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}

		}).checkReturnCode();
	}

}
