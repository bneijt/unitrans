package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.session.SessionService;
import nl.bneijt.unitrans.session.elements.Session;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Path("/meta")
public class MetaDataResource {

    private final SessionService sessionService;
    private final MetadataService metadataService;

    @Inject
    public MetaDataResource(SessionService sessionService, MetadataService metadataService) {
        this.sessionService = sessionService;
        this.metadataService = metadataService;
    }

    @GET
    @Path("{sessionId}/{metadataId}")
    public Response get(@PathParam("sessionId") String sessionId, @PathParam("metadataId") String metadataId) throws IOException {
        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();

        if (metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(metadataService.get(metaIdent)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("Could not reach block").build();
    }
}
