package io.codibase.server.web.resource;

import io.codibase.k8shelper.KubernetesHelper;
import io.codibase.server.CodiBase;
import io.codibase.server.attachment.AttachmentService;
import io.codibase.server.cluster.ClusterService;
import io.codibase.server.service.*;
import io.codibase.server.model.Build;
import io.codibase.server.model.Issue;
import io.codibase.server.model.Project;
import io.codibase.server.security.SecurityUtils;
import io.codibase.server.util.CryptoUtils;
import io.codibase.server.util.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.tika.mime.MimeTypes;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URLEncoder;

import static io.codibase.commons.utils.LockUtils.read;
import static io.codibase.server.util.IOUtils.BUFFER_SIZE;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AttachmentResource extends AbstractResource {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_PROJECT = "project";
	
	private static final String PARAM_ATTACHMENT_GROUP = "attachment-group";
	
	private static final String PARAM_ATTACHMENT = "attachment";
	
	public static final String PARAM_AUTHORIZATION = "authorization";
	
	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		PageParameters params = attributes.getParameters();
		
		Long projectId = params.get(PARAM_PROJECT).toLong();
		String attachmentGroup = params.get(PARAM_ATTACHMENT_GROUP).toString();
		
		if (StringUtils.isBlank(attachmentGroup))
			throw new IllegalArgumentException("Parameter 'attachment-group' has to be specified");
		else if (attachmentGroup.contains(".."))
			throw new IllegalArgumentException("Invalid parameter 'attachment-group'");

		if (!SecurityUtils.isSystem()) {
			Project project = CodiBase.getInstance(ProjectService.class).load(projectId);
			
			String authorization = params.get(PARAM_AUTHORIZATION).toOptionalString();
			if (authorization == null 
					|| !new String(CryptoUtils.decrypt(Base64.decodeBase64(authorization)), UTF_8).equals(attachmentGroup)) {
				Issue issue;
				Build build;
				if (CodiBase.getInstance(PullRequestService.class).find(attachmentGroup) != null
						|| CodiBase.getInstance(CodeCommentService.class).findByUUID(attachmentGroup) != null) {
					if (!SecurityUtils.canReadCode(project))
						throw new UnauthorizedException();
				} else if ((issue = CodiBase.getInstance(IssueService.class).find(attachmentGroup)) != null) {
					if (!SecurityUtils.canAccessIssue(issue))
						throw new UnauthorizedException();
				} else if ((build = CodiBase.getInstance(BuildService.class).find(attachmentGroup)) != null) {
					if (!SecurityUtils.canAccessBuild(build))
						throw new UnauthorizedException();
				} else if (!SecurityUtils.canAccessProject(project)) {
					throw new UnauthorizedException();
				}
			}
		}

		String attachment = params.get(PARAM_ATTACHMENT).toString();
		if (StringUtils.isBlank(attachment))
			throw new IllegalArgumentException("attachment parameter has to be specified");
		else if (attachment.contains(".."))
			throw new IllegalArgumentException("Invalid attachment parameter");

		ResourceResponse response = new ResourceResponse();
		response.setContentLength(getAttachmentService().getAttachmentInfo(projectId, attachmentGroup, attachment).getLength());
		
		response.getHeaders().addHeader("X-Content-Type-Options", "nosniff");
		response.setContentType(MimeTypes.OCTET_STREAM);

		response.setFileName(URLEncoder.encode(attachment, UTF_8));

		response.setWriteCallback(new WriteCallback() {

			@Override
			public void writeData(Attributes attributes) throws IOException {
				String activeServer = getProjectService().getActiveServer(projectId, true);
				ClusterService clusterService = CodiBase.getInstance(ClusterService.class);
				if (activeServer.equals(clusterService.getLocalServerAddress())) {
					read(getAttachmentService().getAttachmentLockName(projectId, attachmentGroup), () -> {
						File attachmentFile = new File(getAttachmentService().getAttachmentGroupDir(projectId, attachmentGroup), attachment);
						try (
								InputStream is = new FileInputStream(attachmentFile);
								OutputStream os = attributes.getResponse().getOutputStream()) {
							IOUtils.copy(is, os, BUFFER_SIZE);
						}
						return null;						
					});
				} else {
	    			Client client = ClientBuilder.newClient();
	    			try {
	    				CharSequence path = RequestCycle.get().urlFor(
	    						new AttachmentResourceReference(), 
	    						AttachmentResource.paramsOf(projectId, attachmentGroup, attachment));
	    				String activeServerUrl = clusterService.getServerUrl(activeServer) + path;
	    				
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
		
	private ProjectService getProjectService() {
		return CodiBase.getInstance(ProjectService.class);
	}
	
	private static AttachmentService getAttachmentService() {
		return CodiBase.getInstance(AttachmentService.class);
	}

	public static PageParameters paramsOf(Long projectId, String attachmentGroup, String attachment) {
		PageParameters params = new PageParameters();
		params.set(PARAM_PROJECT, projectId);
		params.set(PARAM_ATTACHMENT_GROUP, attachmentGroup);
		params.set(PARAM_ATTACHMENT, attachment);
		
		params.set("v", getAttachmentService().getAttachmentInfo(projectId, attachmentGroup, attachment).getLastModified());
		
		return params;
	}

	public static String authorizeGroup(String attachmentUrl) {
		try {
			URIBuilder builder = new URIBuilder(attachmentUrl);
			if (builder.getPathSegments().size() >= 5) {
				String group = builder.getPathSegments().get(4);
				byte[] encrypted = CryptoUtils.encrypt(group.getBytes(UTF_8));
				String base64 = Base64.encodeBase64URLSafeString(encrypted);
				builder.addParameter(PARAM_AUTHORIZATION, base64);
			}
			return builder.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
}
