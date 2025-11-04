package io.codibase.server.rest.resource;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.shiro.authz.UnauthorizedException;

import io.codibase.server.data.migration.VersionedXmlDoc;
import io.codibase.server.service.AuditService;
import io.codibase.server.service.PackService;
import io.codibase.server.model.Pack;
import io.codibase.server.model.PackBlob;
import io.codibase.server.model.PackBlobReference;
import io.codibase.server.model.PackLabel;
import io.codibase.server.rest.annotation.Api;
import io.codibase.server.rest.resource.support.RestConstants;
import io.codibase.server.search.entity.pack.PackQuery;
import io.codibase.server.security.SecurityUtils;

@Api(name="Package")
@Path("/packages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PackResource {

	private final PackService packService;
	
	private final AuditService auditService;
	
	@Inject
	public PackResource(PackService packService, AuditService auditService) {
		this.packService = packService;
		this.auditService = auditService;
	}

	@Api(order=100)
	@Path("/{packId}")
    @GET
    public Pack getPack(@PathParam("packId") Long packId) {
		Pack pack = packService.load(packId);
    	if (!SecurityUtils.canReadPack(pack.getProject())) 
			throw new UnauthorizedException();
    	return pack;
    }

	@Api(order=150, description = "Get list of <a href='/~help/api/io.codibase.server.rest.PackLabelResource'>labels</a>")
	@Path("/{packId}/labels")
	@GET
	public Collection<PackLabel> getLabels(@PathParam("packId") Long packId) {
		Pack pack = packService.load(packId);
		if (!SecurityUtils.canReadPack(pack.getProject()))
			throw new UnauthorizedException();
		return pack.getLabels();
	}
	
	@Api(order=300)
	@Path("/{packId}/blobs")
    @GET
    public Collection<PackBlob> getBlobs(@PathParam("packId") Long packId) {
		Pack pack = packService.load(packId);
    	if (!SecurityUtils.canReadPack(pack.getProject())) 
			throw new UnauthorizedException();
    	return pack.getBlobReferences().stream().map(PackBlobReference::getPackBlob).collect(toList());
    }
	
	@Api(order=600)
	@GET
    public List<Pack> queryPacks(
    		@QueryParam("query") @Api(description="Syntax of this query is the same as in <a href='/~packages'>packages page</a>", example="\"Type\" is \"Container Image\"") String query, 
    		@QueryParam("offset") @Api(example="0") int offset, 
    		@QueryParam("count") @Api(example="100") int count) {
		var subject = SecurityUtils.getSubject();
		if (!SecurityUtils.isAdministrator(subject) && count > RestConstants.MAX_PAGE_SIZE)
    		throw new NotAcceptableException("Count should not be greater than " + RestConstants.MAX_PAGE_SIZE);

    	PackQuery parsedQuery;
		try {
			parsedQuery = PackQuery.parse(null, query, true);
		} catch (Exception e) {
			throw new NotAcceptableException("Error parsing query", e);
		}
    	
    	return packService.query(subject, null, parsedQuery, false, offset, count);
    }
	
	@Api(order=700)
	@Path("/{packId}")
    @DELETE
    public Response deletePack(@PathParam("packId") Long packId) {
    	Pack pack = packService.load(packId);
    	if (!SecurityUtils.canWritePack(pack.getProject()))
			throw new UnauthorizedException();
    	packService.delete(pack);
		var oldAuditContent = VersionedXmlDoc.fromBean(pack).toXML();
		auditService.audit(pack.getProject(), "deleted package \"" + pack.getReference(false) + "\" via RESTful API", oldAuditContent, null);
    	return Response.ok().build();
    }
	
}
