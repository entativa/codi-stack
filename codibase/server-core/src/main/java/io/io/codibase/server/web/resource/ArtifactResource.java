package io.codibase.server.web.resource;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.codibase.commons.utils.ExplicitException;
import io.codibase.commons.utils.StringUtils;
import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.server.CodiBase;
import io.codibase.server.cluster.ClusterService;
import io.codibase.server.service.BuildService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.model.Build;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.IOUtils;
import io.codibase.server.util.artifact.FileInfo;
import io.codibase.server.web.util.MimeUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.codibase.commons.utils.LockUtils.read;
import static io.codibase.server.model.Build.getArtifactsLockName;
import static io.codibase.server.util.IOUtils.BUFFER_SIZE;

public class ArtifactResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";

	private static final String PARAM_BUILD = "build";

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		Long projectId = params.get(PARAM_PROJECT).toLong();
		Long buildNumber = params.get(PARAM_BUILD).toLong();
		
		List<String> pathSegments = new ArrayList<>();

		for (int i = 0; i < params.getIndexedCount(); i++) {
			String pathSegment = params.get(i).toString();
			if (pathSegment.contains(".."))
				throw new ExplicitException("Invalid request path");
			if (pathSegment.length() != 0)
				pathSegments.add(pathSegment);
		}
		
		if (pathSegments.isEmpty())
			throw new ExplicitException("Artifact path has to be specified");
		
		String artifactPath = Joiner.on("/").join(pathSegments);
		
		FileInfo fileInfo = null;
		if (!SecurityUtils.isSystem()) {
			Project project = CodiBase.getInstance(ProjectService.class).load(projectId);
			
			Build build = CodiBase.getInstance(BuildService.class).find(project, buildNumber);

			if (build == null) {
				String message = String.format("Unable to find build (project: %s, build number: %d)", 
						project.getPath(), buildNumber);
				throw new EntityNotFoundException(message);
			}
			
			if (!SecurityUtils.canAccessBuild(build))
				throw new UnauthorizedException();
			
			fileInfo = (FileInfo) getBuildService().getArtifactInfo(build, artifactPath);
		}
		
		ResourceResponse response = new ResourceResponse();
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.disableCaching();

		String fileName = artifactPath;
		if (fileName.contains("/"))
			fileName = StringUtils.substringAfterLast(fileName, "/");
		try {
			response.setFileName(URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		if (fileInfo != null) {
			response.setContentLength(fileInfo.getLength());
			response.setContentType(MimeUtils.sanitize(fileInfo.getMediaType()));
		} else {
			response.setContentType(MimeTypes.OCTET_STREAM);
		}
		
		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				ProjectService projectService = CodiBase.getInstance(ProjectService.class);
				String activeServer = projectService.getActiveServer(projectId, true);
				ClusterService clusterService = CodiBase.getInstance(ClusterService.class);
				if (activeServer.equals(clusterService.getLocalServerAddress())) {
					read(getArtifactsLockName(projectId, buildNumber), () -> {
						File artifactFile = new File(getBuildService().getArtifactsDir(projectId, buildNumber), artifactPath);
						try (
								InputStream is = new FileInputStream(artifactFile);
								OutputStream os = attributes.getResponse().getOutputStream()) {
							IOUtils.copy(is, os, BUFFER_SIZE);
						}
						return null;
					});
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new ArtifactResourceReference(), 
	    						ArtifactResource.paramsOf(projectId, buildNumber, artifactPath));
	    				String activeServerUrl = clusterService.getServerUrl(activeServer);
	    				
	    				WebTarget target = client.target(activeServerUrl).path(path.toString());
	    				Invocation.Builder builder =  target.request();
	    				builder.header(HttpHeaders.AUTHORIZATION, 
	    						KubernetesHelper.BEARER + " " + clusterService.getCredential());
	    				
	    				try (Response response = builder.get()) {
	    					KubernetesHelper.checkStatus(response);
	    					try (
	    							InputStream is = response.readEntity(InputStream.class);
	    							OutputStream os = attributes.getResponse().getOutputStream()) {
	    						IOUtils.copy(is, os, BUFFER_SIZE);
	    					} 
	    				} 
	    			} finally {
	    				client.close();
	    			}
				}
			}			
			
		});

		return response;
	}
	
	private BuildService getBuildService() {
		return CodiBase.getInstance(BuildService.class);
	}
	
	public static PageParameters paramsOf(Long projectId, Long buildNumber, String path) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_BUILD, buildNumber);
		
		int index = 0;
		for (String segment: Splitter.on("/").split(path)) {
			params.set(index, segment);
			index++;
		}
		return params;
	}

}
