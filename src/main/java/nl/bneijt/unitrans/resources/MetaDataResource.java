package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.accesscontrol.AccessControl;
import nl.bneijt.unitrans.accesscontrol.User;
import nl.bneijt.unitrans.blockstore.BlockStore;
import nl.bneijt.unitrans.blockstore.Digest;
import nl.bneijt.unitrans.blockstore.Hash;
import nl.bneijt.unitrans.blockstore.MetaData;
import nl.bneijt.unitrans.resources.elements.MetaDataElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/metadata")
public class MetaDataResource {
    private final BlockStore blockStore;
    private final ResourceProtection resourceProtection;
    private final AccessControl accessControl;


    private Logger logger = LoggerFactory.getLogger(MetaDataResource.class);

    @Inject
    public MetaDataResource(BlockStore blockStore, ResourceProtection resourceProtection, AccessControl accessControl) {
        this.blockStore = blockStore;
        this.resourceProtection = resourceProtection;
        this.accessControl = accessControl;
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") String hash) throws IOException {
        if (hash.length() != Digest.HEX_SIZE) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Wrong block hash length").build();
        }
        Hash blockHash = Hash.fromBase16(hash);
        MetaData metaData = blockStore.readMeta(blockHash);
        if (metaData != null) {
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(MetaDataElement.from(metaData, blockHash)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find block").build();
        }
    }

    @GET
    @Path("root")
    public Response get(@Context HttpServletRequest request) throws IOException {
        User user = resourceProtection.getUser(request);

        MetaDataElement metaDataElement = getOrEmptyRootMetadataBlock(user);

        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(metaDataElement).build();
    }

    private MetaDataElement getOrEmptyRootMetadataBlock(User user) throws IOException {
        Hash rootIdentifier = accessControl.getRootMetaBlockFor(user);
        if(rootIdentifier == null) {
            //Create a new empty root metadatablock
            MetaData emptyRootMetadata = new MetaData();
            rootIdentifier = blockStore.writeMeta(emptyRootMetadata);
            accessControl.setRootMetaBlockFor(user, rootIdentifier);
            return MetaDataElement.from(emptyRootMetadata, rootIdentifier);
        } else {
            MetaData metaData = blockStore.readMeta(rootIdentifier);
            return MetaDataElement.from(metaData, rootIdentifier);
        }


    }

}
