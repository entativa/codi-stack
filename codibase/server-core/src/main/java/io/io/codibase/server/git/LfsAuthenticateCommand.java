/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.codibase.server.git;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.StringUtils;
import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.server.CodiBase;
import io.codibase.server.service.AccessTokenService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.persistence.SessionService;
import io.codibase.server.ssh.SshAuthenticator;
import io.codibase.server.util.CollectionUtils;
import io.codibase.server.web.UrlService;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.session.ServerSessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class LfsAuthenticateCommand implements Command, ServerSessionAware {

	static final String COMMAND_PREFIX = "git-lfs-authenticate";
	
	private static final Logger logger = LoggerFactory.getLogger(LfsAuthenticateCommand.class);
	
	private final String commandString;
	
    private OutputStream out;
    
    private OutputStream err;
    
    private ExitCallback callback;
    
	private ServerSession session;

	LfsAuthenticateCommand(String commandString) {
		this.commandString = commandString;
	}
	
    @Override
    public void setInputStream(InputStream in) {
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
    	this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    @Override
    public void start(ChannelSession channel, Environment env) throws IOException {
    	SshAuthenticator authenticator = CodiBase.getInstance(SshAuthenticator.class);
    	Long userId = authenticator.getPublicKeyOwnerId(session);
    	CodiBase.getInstance(ExecutorService.class).submit(() -> {
			SessionService sessionService = CodiBase.getInstance(SessionService.class);
			sessionService.openSession(); 
			try {
				String accessToken = CodiBase.getInstance(AccessTokenService.class).createTemporal(userId, 300);
				String projectPath = StringUtils.strip(StringUtils.substringBefore(
						commandString.substring(COMMAND_PREFIX.length()+1), " "), "/\\");
				
				var projectService = CodiBase.getInstance(ProjectService.class);
				var project = projectService.findByPath(projectPath);
				if (project == null && projectPath.endsWith(".git")) {
					projectPath = StringUtils.substringBeforeLast(projectPath, ".");
					project = projectService.findByPath(projectPath);
				}
				if (project == null)
					throw new ExplicitException("Project not found: " + projectPath);
				String url = CodiBase.getInstance(UrlService.class).cloneUrlFor(project, false);
				Map<Object, Object> response = CollectionUtils.newHashMap(
						"href", url + ".git/info/lfs", 
						"header", CollectionUtils.newHashMap(
								"Authorization", KubernetesHelper.BEARER + " " + accessToken)); 
				out.write(CodiBase.getInstance(ObjectMapper.class).writeValueAsBytes(response));
				callback.onExit(0);
			} catch (Exception e) {
				logger.error("Error executing " + COMMAND_PREFIX, e);
				new PrintStream(err).println("Check server log for details");
				callback.onExit(-1);
			} finally {                
				sessionService.closeSession();
			}
		});
    }

    @Override
    public void destroy(ChannelSession channel) {
    	
    }

	@Override
	public void setSession(ServerSession session) {
		this.session = session;
	}

}
