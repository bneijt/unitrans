package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.accesscontrol.AccessControl;
import nl.bneijt.unitrans.accesscontrol.User;
import nl.bneijt.unitrans.blockstore.BlockStore;
import nl.bneijt.unitrans.blockstore.Digest;
import nl.bneijt.unitrans.blockstore.Hash;
import nl.bneijt.unitrans.blockstore.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Path("/block")
public class BlockResource {
    private final BlockStore blockStore;
    private final ResourceProtection resourceProtection;
    private final AccessControl accessControl;

    private Logger logger = LoggerFactory.getLogger(BlockResource.class);

    @Inject
    public BlockResource(BlockStore blockStore, ResourceProtection resourceProtection, AccessControl accessControl) {
        this.blockStore = blockStore;
        this.resourceProtection = resourceProtection;
        this.accessControl = accessControl;
    }

    @POST
    public Response post(@Context HttpServletRequest request, InputStream inputStream) throws IOException {
        User user = resourceProtection.getUser(request);
        resourceProtection.throwIfNotAllowedToWrite(user);
        blockStore.writeBlock(inputStream);
        return Response.ok().build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String hash) throws IOException {
        if (hash.length() != Digest.HEX_SIZE) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Wrong block hash length").build();
        }
        Hash blockHash = Hash.fromBase16(hash);
        InputStream blockStream = blockStore.openBlock(blockHash);
        if (blockStream != null) {
            return Response.ok().type(MediaType.APPLICATION_OCTET_STREAM_TYPE).entity(blockStream).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find block").build();
        }
    }
}
