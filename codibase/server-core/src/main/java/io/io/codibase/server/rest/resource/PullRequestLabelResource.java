package io.codibase.server.rest.resource;

import io.codibase.server.service.PullRequestLabelService;
import io.codibase.server.model.PullRequestLabel;
import io.codibase.server.rest.annotation.Api;
import io.codibase.server.security.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/pull-request-labels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PullRequestLabelResource {

	private final PullRequestLabelService pullRequestLabelService;

	@Inject
	public PullRequestLabelResource(PullRequestLabelService pullRequestLabelService) {
		this.pullRequestLabelService = pullRequestLabelService;
	}
	
	@Api(order=200, description="Create pull request label")
	@POST
	public Long create(@NotNull PullRequestLabel pullRequestLabel) {
		if (!SecurityUtils.canModifyPullRequest(pullRequestLabel.getRequest()))
			throw new UnauthorizedException();
		pullRequestLabelService.create(pullRequestLabel);
		return pullRequestLabel.getId();
	}
	
	@Api(order=300)
	@Path("/{pullRequestLabelId}")
	@DELETE
	public Response delete(@PathParam("pullRequestLabelId") Long pullRequestLabelId) {
		PullRequestLabel pullRequestLabel = pullRequestLabelService.load(pullRequestLabelId);
		if (!SecurityUtils.canModifyPullRequest(pullRequestLabel.getRequest()))
			throw new UnauthorizedException();
		pullRequestLabelService.delete(pullRequestLabel);
		return Response.ok().build();
	}
	
}
