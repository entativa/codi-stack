package io.codibase.server.git.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.codibase.commons.utils.command.Commandline;
import io.codibase.commons.utils.command.LineConsumer;
import io.codibase.server.git.CommandUtils;

public class GetRawCommitCommand {

	private static final Logger logger = LoggerFactory.getLogger(GetRawCommitCommand.class);
	
	private final File workingDir;
	
	private final String revision;
	
	private final Map<String, String> envs;
	
	public GetRawCommitCommand(File workingDir, String revision, Map<String, String> envs) {
		this.workingDir = workingDir;
		this.revision = revision;
		this.envs = envs;
	}

	protected Commandline newGit() {
		return CommandUtils.newGit();
	}
	
	public byte[] run() {
		Commandline git = newGit().workingDir(workingDir);
		git.environments().putAll(envs);
		
		git.addArgs("cat-file", "commit", revision);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		git.execute(baos, new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		}).checkReturnCode();

		return baos.toByteArray();
	}

}