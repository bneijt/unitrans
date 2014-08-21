package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.accesscontrol.User;
import nl.bneijt.unitrans.blockstore.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("/download/file/")
public class DownloadResource {
    private final BlockStore blockStore;
    private final StreamStore streamStore;
    private final ResourceProtection resourceProtection;

    @Inject
    public DownloadResource(BlockStore blockStore, StreamStore streamStore, ResourceProtection resourceProtection) {
        this.blockStore = blockStore;
        this.streamStore = streamStore;
        this.resourceProtection = resourceProtection;
    }

    @GET
    @Path("{id}")
    public Response get(@Context HttpServletRequest request, @PathParam("id") String hash) throws IOException {
        User user = resourceProtection.getUser(request);
        if (hash.length() != Digest.HEX_SIZE) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Wrong block hash length").build();
        }
        Hash blockHash = Hash.fromBase16(hash);

        MetaData metaData = blockStore.readMeta(blockHash);

        //Read all blocks below the meta block, using depth first
        InputStream inputStream = streamStore.readStream(metaData);


        if (inputStream != null) {
            return Response.ok().type(MediaType.APPLICATION_OCTET_STREAM_TYPE).entity(inputStream).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not open blocks under " + hash).build();
        }
    }
}
