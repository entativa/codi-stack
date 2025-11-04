package io.codibase.server.web.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import io.codibase.commons.bootstrap.Bootstrap;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.TarUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentService;
import io.codibase.server.service.AgentTokenService;
import io.codibase.server.security.SecurityUtils;

public class AgentLibResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		String bearerToken = SecurityUtils.getBearerToken(
				(HttpServletRequest) attributes.getRequest().getContainerRequest());
		if (bearerToken != null 
				&& CodiBase.getInstance(AgentTokenService.class).find(bearerToken) != null) {
			ResourceResponse response = new ResourceResponse();
			response.setContentType(MimeTypes.OCTET_STREAM);
			response.disableCaching();
			
			response.setWriteCallback(new WriteCallback() {

				@Override
				public void writeData(Attributes attributes) throws IOException {
					File tempDir = FileUtils.createTempDir("agent-lib");
					try {
						Collection<String> agentLibs = CodiBase.getInstance(AgentService.class).getAgentLibs();
						
						for (File file: Bootstrap.getBootDir().listFiles()) {
							if (agentLibs.contains(file.getName())) 
								FileUtils.copyFileToDirectory(file, tempDir);
						}
						
						for (File file: Bootstrap.getLibDir().listFiles()) {
							if (agentLibs.contains(file.getName())) 
								FileUtils.copyFileToDirectory(file, tempDir);
						}
						
						try(var os = attributes.getResponse().getOutputStream()) {
							TarUtils.tar(tempDir, os, false);
						}
					} finally {
						FileUtils.deleteDir(tempDir);
					}
				}				
				
			});

			return response;				
		} else {
			throw new ExplicitException("A valid agent token is expected");
		}
	}

}
