package io.codibase.server.web.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.resource.AbstractResource;

import com.google.common.collect.Sets;

import io.codibase.agent.Agent;
import io.codibase.commons.bootstrap.Bootstrap;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.FileUtils;
import io.codibase.commons.utils.StringUtils;
import io.codibase.commons.utils.TarUtils;
import io.codibase.commons.utils.ZipUtils;
import io.codibase.server.CodiBase;
import io.codibase.server.service.AgentService;
import io.codibase.server.service.AgentTokenService;
import io.codibase.server.service.SettingService;
import io.codibase.server.model.AgentToken;
import io.codibase.server.security.SecurityUtils;

public class AgentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		if (!SecurityUtils.isAdministrator()) 
			throw new UnauthorizedException();
		
		if (!new File(Bootstrap.installDir, "agent").exists())
			throw new ExplicitException("No agent package to download");

		ResourceResponse response = new ResourceResponse();
		response.setContentType(MimeTypes.OCTET_STREAM);
		response.disableCaching();
		
		String fileName = StringUtils.substringAfterLast(
				attributes.getRequest().getUrl().getPath(), "/");
		response.setFileName(fileName);
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				File tempDir = FileUtils.createTempDir("agent");
				try {
					String agentVersion = CodiBase.getInstance(AgentService.class).getAgentVersion();
					
					File agentDir = new File(tempDir, "agent");
					FileUtils.copyDirectoryToDirectory(new File(Bootstrap.installDir, "agent"), agentDir);
					
					String wrapperConfContent = FileUtils.readFileToString(
							new File(agentDir, "agent/conf/wrapper.conf"), StandardCharsets.UTF_8);
					wrapperConfContent = wrapperConfContent.replace("agentVersion", agentVersion);
					FileUtils.writeStringToFile(new File(agentDir, "agent/conf/wrapper.conf"), 
							wrapperConfContent, StandardCharsets.UTF_8);
					
					Properties props = new Properties();
					props.setProperty("serverUrl", CodiBase.getInstance(SettingService.class).getSystemSetting().getServerUrl());
					
					AgentToken token = new AgentToken();
					CodiBase.getInstance(AgentTokenService.class).createOrUpdate(token);
					props.setProperty("agentToken", token.getValue());
					
					try (var os = new BufferedOutputStream(new FileOutputStream(new File(agentDir, "agent/conf/agent.properties")))) {
						String comment = "For a list of supported agent properties, please visit:\n" 
								+ "https://docs.codibase.io/administration-guide/agent-management#agent-propertiesenvironments";
						props.store(os, comment);
					}
					
					try (
							var is = Agent.class.getClassLoader().getResourceAsStream("agent/conf/logback.xml");
							var os = new BufferedOutputStream(new FileOutputStream(new File(agentDir, "agent/conf/logback.xml")))) {
						IOUtils.copy(is, os);
					}
					FileUtils.touchFile(new File(agentDir, "agent/conf/attributes.properties"));
					FileUtils.touchFile(new File(agentDir, "agent/logs/console.log"));
					
					Collection<String> agentLibs = CodiBase.getInstance(AgentService.class).getAgentLibs();
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (file.getName().startsWith("libwrapper-") 
								|| file.getName().startsWith("wrapper-") 
								|| file.getName().equals("wrapper.jar")) {
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/boot"));
						}
					}
					
					for (File file: Bootstrap.getBootDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/lib/" + agentVersion));
					}
					for (File file: Bootstrap.getLibDir().listFiles()) {
						if (agentLibs.contains(file.getName())) 
							FileUtils.copyFileToDirectory(file, new File(agentDir, "agent/lib/" + agentVersion));
					}

					if (fileName.endsWith("zip")) {
						File packageFile = new File(tempDir, fileName);
						ZipUtils.zip(agentDir, packageFile, "agent/boot/wrapper-*, agent/bin/*.sh");
						try (var is = new FileInputStream(packageFile)) {
							IOUtils.copy(is, attributes.getResponse().getOutputStream());
						}
					} else {
						try (var os = attributes.getResponse().getOutputStream()) {
							TarUtils.tar(agentDir, Sets.newHashSet("**"), Sets.newHashSet(),
									Sets.newHashSet("agent/boot/wrapper-*", "agent/bin/*.sh"),
									os, true);
						}
					}
				} finally {
					FileUtils.deleteDir(tempDir);
				}
			}				
			
		});

		return response;
	}

}
