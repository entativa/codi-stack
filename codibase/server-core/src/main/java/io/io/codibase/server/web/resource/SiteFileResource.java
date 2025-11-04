package io.codibase.server.web.resource;

import static io.codibase.commons.utils.LockUtils.read;
import static io.codibase.server.util.IOUtils.copyRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import io.codibase.commons.utils.ExplicitException;
import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.server.CodiBase;
import io.codibase.server.cluster.ClusterService;
import io.codibase.server.service.ProjectService;
import io.codibase.server.exception.ExceptionUtils;
import io.codibase.server.git.BlobIdent;
import io.codibase.server.model.Project;
import io.codibase.server.util.LongRange;
import io.codibase.server.util.artifact.ArtifactInfo;
import io.codibase.server.util.artifact.DirectoryInfo;
import io.codibase.server.util.artifact.FileInfo;
import io.codibase.server.web.mapper.ProjectMapperUtils;
import io.codibase.server.web.util.WicketUtils;

public class SiteFileResource extends AbstractResource {

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LoggerFactory.getLogger(SiteFileResource.class);

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();

		String projectPath = params.get(ProjectMapperUtils.PARAM_PROJECT).toString();
		Project project = getProjectService().findByPath(projectPath);
		if (project == null)
			throw new EntityNotFoundException();
		
		Long projectId = project.getId();
		
		List<String> filePathSegments = new ArrayList<>();
		for (int i = 0; i < params.getIndexedCount(); i++) {
			String segment = params.get(i).toString();
			if (segment.contains(".."))
				throw new ExplicitException("Invalid request path");
			if (segment.length() != 0)
				filePathSegments.add(segment);
		}
		
		FileInfo fileInfo;
		String filePath = Joiner.on("/").join(filePathSegments);
		if (filePath.length() != 0) {
			ArtifactInfo artifactInfo = getProjectService().getSiteArtifactInfo(projectId, filePath);
			if (artifactInfo instanceof DirectoryInfo) {
				if (attributes.getRequest().getUrl().getPath().endsWith("/")) {
					filePath += "/index.html";
					artifactInfo = getProjectService().getSiteArtifactInfo(projectId, filePath);
					if (artifactInfo instanceof FileInfo)
						fileInfo = (FileInfo) artifactInfo;
					else						
						return newNotFoundResponse(filePath);
				} else {
					throw new RedirectToUrlException(attributes.getRequest().getUrl().getPath() + "/");
				}
			} else if (artifactInfo instanceof FileInfo) {
				fileInfo = (FileInfo) artifactInfo;
			} else {
				return newNotFoundResponse(filePath);
			}
		} else if (attributes.getRequest().getUrl().getPath().endsWith("/")) {
			filePath = "index.html";
			ArtifactInfo artifactInfo = getProjectService().getSiteArtifactInfo(projectId, filePath);
			if (artifactInfo instanceof FileInfo)
				fileInfo = (FileInfo) artifactInfo;
			else
				return newNotFoundResponse(filePath);
		} else {
			throw new RedirectToUrlException(attributes.getRequest().getUrl().getPath() + "/");
		}
		
		ResourceResponse response = new ResourceResponse();
		response.setAcceptRange(ContentRangeType.BYTES);
		response.setContentType(fileInfo.getMediaType());
		response.setContentLength(fileInfo.getLength());
		
		try {
			response.setFileName(URLEncoder.encode(filePath, StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		String finalFilePath = filePath;
		response.setWriteCallback(new WriteCallback() {

			private void handle(Exception e) {
				EofException eofException = ExceptionUtils.find(e, EofException.class);
				if (eofException != null) 
					logger.trace("EOF while writing data", eofException);
				else 
					throw ExceptionUtils.unchecked(e);
			}
			
			@Override
			public void writeData(Attributes attributes) throws IOException {
				LongRange range = WicketUtils.getRequestContentRange(fileInfo.getLength());
				
				String activeServer = getProjectService().getActiveServer(projectId, true);
				if (activeServer.equals(getClusterService().getLocalServerAddress())) {
					read(Project.getSiteLockName(projectId), () -> {
						File file = new File(getProjectService().getSiteDir(projectId), finalFilePath);
						try (InputStream is = new FileInputStream(file)) {
							copyRange(is, attributes.getResponse().getOutputStream(), range);
						} catch (IOException e) {
							handle(e);
						}
						return null;
					});
				} else {
					Client client = ClientBuilder.newClient();
					try {
						String serverUrl = getClusterService().getServerUrl(activeServer);
						WebTarget target = client.target(serverUrl);
						target = target.path("~api/cluster/site")
								.queryParam("projectId", project.getId())
								.queryParam("filePath", finalFilePath);
						Invocation.Builder builder =  target.request();
						builder.header(HttpHeaders.AUTHORIZATION, 
								KubernetesHelper.BEARER + " " + getClusterService().getCredential());
						try (Response response = builder.get()){
							KubernetesHelper.checkStatus(response);
							try (InputStream is = response.readEntity(InputStream.class)) {
								copyRange(is, attributes.getResponse().getOutputStream(), range);
							} catch (Exception e) {
								handle(e);
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
	
	private ResourceResponse newNotFoundResponse(String filePath) {
		ResourceResponse response = new ResourceResponse();
		response.setStatusCode(HttpServletResponse.SC_NOT_FOUND).setContentType(MediaType.TEXT_PLAIN);
		return new ResourceResponse().setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				attributes.getResponse().write("Site file not found: " + filePath);
			}
			
		});			
	}

	private ProjectService getProjectService() {
		return CodiBase.getInstance(ProjectService.class);
	}
	
	private ClusterService getClusterService() {
		return CodiBase.getInstance(ClusterService.class);
	}
	
	public static PageParameters paramsOf(Project project, BlobIdent blobIdent) {
		PageParameters params = new PageParameters();
		params.set(ProjectMapperUtils.PARAM_PROJECT, project.getPath());
		
		int index = 0;
		for (String segment: Splitter.on("/").split(blobIdent.revision)) {
			params.set(index, segment);
			index++;
		}
		for (String segment: Splitter.on("/").split(blobIdent.path)) {
			params.set(index, segment);
			index++;
		}

		return params;
	}

}
