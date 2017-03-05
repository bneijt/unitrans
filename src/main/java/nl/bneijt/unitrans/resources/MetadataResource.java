package nl.bneijt.unitrans.resources;

import com.google.common.collect.Iterables;
import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.session.SessionService;
import nl.bneijt.unitrans.session.elements.Session;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Path("/meta")
public class MetadataResource {

    private final SessionService sessionService;
    private final MetadataService metadataService;

    @Inject
    public MetadataResource(SessionService sessionService, MetadataService metadataService) {
        this.sessionService = sessionService;
        this.metadataService = metadataService;
    }

    @GET
    @Path("{sessionId}/{metadataId}")
    public Response get(@PathParam("sessionId") String sessionId,
                        @PathParam("metadataId") String metadataId) throws IOException {

        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();

        if (metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(metadataService.get(metaIdent)).build();
        }
        return JsonResponse.unauthorized("Could not reach block");
    }

    @POST
    @Path("{sessionId}/{metadataId}/append")
    public Response append(@PathParam("sessionId") String sessionId,
                        @PathParam("metadataId") String metadataId) throws IOException, URISyntaxException {

        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();

        if (!metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            return JsonResponse.unauthorized("Could not reach block");
        }
        MetadataBlock targetBlock = metadataService.get(metaIdent);
        MetadataBlock newMetadataBlock = MetadataBlock.emptyRandomBlock();
        //TODO Read JSON post body for properties to add to the metadata block
        MetadataBlock newTargetBlock = metadataService.appendMetadata(targetBlock, newMetadataBlock);

        List<MetadataBlock> newBlocksLeadingToNewRoot = metadataService.reRoot(session.rootBlock, targetBlock, newMetadataBlock);
        metadataService.write(newBlocksLeadingToNewRoot);
        session = sessionService.reRoot(session, Iterables.getLast(newBlocksLeadingToNewRoot).ident);

        //On to the new metadata block in the new session
        return Response.seeOther(new URI(session.ident.toString() + "/" + session.rootBlock + "/" + newTargetBlock.ident.toString())).build();

    }

}
