package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.blockstore.BlockService;
import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.session.SessionService;
import nl.bneijt.unitrans.session.elements.Session;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Path("/data")
public class DataResource {
    private final BlockService blockService;
    private final SessionService sessionService;
    private final MetadataService metadataService;

    @Inject
    public DataResource(BlockService blockService, SessionService sessionService, MetadataService metadataService) {
        this.blockService = blockService;
        this.sessionService = sessionService;
        this.metadataService = metadataService;
    }

    @POST
    @Path("{sessionId}/{metadataId}")
    public Response post(
            @Context HttpServletRequest request,
            @PathParam("sessionId") String sessionId,
            @PathParam("metadataId") String metadataId,
            InputStream inputStream) throws IOException, URISyntaxException {

        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();

        if (metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            MetadataBlock targetBlock = metadataService.get(metaIdent);
            MetadataBlock newTargetBlock = blockService.appendData(targetBlock, inputStream);

            List<MetadataBlock> newBlocks = metadataService.reRoot(session.rootBlock, targetBlock, newTargetBlock);
            metadataService.write(newBlocks);
            Session newSession = sessionService.reRoot(session, newBlocks.get(newBlocks.size() -1).ident);

            return Response.temporaryRedirect(new URI( "../" + newSession.ident.toString() + "/" + session.rootBlock.toString())).build();

        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Could not reach block").build();
    }

    @GET
    @Path("{sessionId}/{metadataId}/{blockId}")
    public Response get(@PathParam("sessionId") String sessionId,
                        @PathParam("metadataId") String metadataId,
                        @PathParam("blockId") String blockId) throws IOException {
        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();

        if (metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(metadataService.get(metaIdent)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).entity("Could not reach block").build();
    }
}
