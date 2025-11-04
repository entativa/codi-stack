package io.codibase.server.rest.resource;

import static io.codibase.server.security.SecurityUtils.canAccessIssue;
import static io.codibase.server.security.SecurityUtils.canModifyOrDelete;
import static io.codibase.server.security.SecurityUtils.getUser;
import static io.codibase.server.security.SecurityUtils.isAdministrator;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.codibase.server.service.IssueCommentService;
import io.codibase.server.service.IssueCommentRevisionService;
import io.codibase.server.model.IssueComment;
import io.codibase.server.model.IssueCommentRevision;
import io.codibase.server.rest.annotation.Api;
import io.codibase.server.security.SecurityUtils;

@Path("/issue-comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class IssueCommentResource {

	private final IssueCommentService commentService;
	
	private final IssueCommentRevisionService commentRevisionService;

	@Inject
	public IssueCommentResource(IssueCommentService commentService, IssueCommentRevisionService commentRevisionService) {
		this.commentService = commentService;
		this.commentRevisionService = commentRevisionService;
	}

	@Api(order=100)
	@Path("/{commentId}")
	@GET
	public IssueComment getComment(@PathParam("commentId") Long commentId) {
		IssueComment comment = commentService.load(commentId);
    	if (!SecurityUtils.canAccessProject(comment.getIssue().getProject()))  
			throw new UnauthorizedException();
    	return comment;
	}
	
	@Api(order=200, description="Create new issue comment")
	@POST
	public Long createComment(@NotNull IssueComment comment) {
		if (!canAccessIssue(comment.getIssue()) 
				|| !isAdministrator() && !comment.getUser().equals(getUser())) {
			throw new UnauthorizedException();
		}
		commentService.create(comment);
		return comment.getId();
	}

	@Api(order=250, description="Update issue comment of specified id")
	@Path("/{commentId}")
	@POST
	public Response updateComment(@PathParam("commentId") Long commentId, @NotNull String content) {
		var comment = commentService.load(commentId);
		if (!canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		var oldContent = comment.getContent();
		if (!oldContent.equals(content)) {
			comment.setContent(content);
			comment.setRevisionCount(comment.getRevisionCount() + 1);
			commentService.update(comment);

			var revision = new IssueCommentRevision();
			revision.setComment(comment);
			revision.setUser(SecurityUtils.getUser());
			revision.setOldContent(oldContent);
			revision.setNewContent(content);
			commentRevisionService.create(revision);
		}
		return Response.ok().build();
	}
	
	@Api(order=300)
	@Path("/{commentId}")
	@DELETE
	public Response deleteComment(@PathParam("commentId") Long commentId) {
		IssueComment comment = commentService.load(commentId);
    	if (!canModifyOrDelete(comment)) 
			throw new UnauthorizedException();
		commentService.delete(SecurityUtils.getUser(), comment);
		return Response.ok().build();
	}
	
}
