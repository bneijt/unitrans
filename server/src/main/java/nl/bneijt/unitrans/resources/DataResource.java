package nl.bneijt.unitrans.resources;

import com.google.common.io.ByteStreams;
import nl.bneijt.unitrans.blockstore.BlockService;
import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.session.SessionService;
import nl.bneijt.unitrans.session.elements.Session;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Path("/data")
public class DataResource {
    final static Logger LOGGER = getLogger(DataResource.class);


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
            LOGGER.info("Stored data, changing metadatablock {} to {}", targetBlock.ident, newTargetBlock.ident);
            return Response.seeOther(new URI( "../" + newSession.ident.toString() + "/" + newTargetBlock.ident.toString())).build();
        }
        return JsonResponse.unauthorized("Could not reach metadatablock");
    }

    @GET
    @Path("{sessionId}/{metadataId}/{datablockId}")
    public Response getBlock(@PathParam("sessionId") String sessionId,
                        @PathParam("metadataId") String metadataId,
                        @PathParam("datablockId") String datablockId) throws IOException {
        UUID metaIdent = UUID.fromString(metadataId);
        UUID sessionIdent = UUID.fromString(sessionId);
        Session session = sessionService.get(sessionIdent).get();
        LOGGER.info("Request for datablock {} from metablock {}", datablockId, metaIdent);

        if (metadataService.reachableFrom(session.rootBlock, metaIdent)) {
            MetadataBlock metadataBlock = metadataService.get(metaIdent);
            //Data must be part of metadata block
            if(metadataBlock.datas.contains(datablockId)) {
                StreamingOutput stream = new StreamingOutput() {
                    @Override
                    public void write(OutputStream os) throws IOException,
                            WebApplicationException {
                        try(InputStream blockInputStream = blockService.open(datablockId)) {
                            ByteStreams.copy(blockInputStream, os);
                        } finally {
                            os.flush();
                        }
                    }
                };
                return Response.ok(stream).type(MediaType.APPLICATION_OCTET_STREAM).build();

            }


        }
        return JsonResponse.unauthorized("Could not reach data block");
    }
}
