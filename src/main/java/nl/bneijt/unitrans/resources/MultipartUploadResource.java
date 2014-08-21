package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.accesscontrol.AccessControl;
import nl.bneijt.unitrans.accesscontrol.User;
import nl.bneijt.unitrans.blockstore.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

@Path("/upload/multipart")
public class MultipartUploadResource {

    Logger logger = LoggerFactory.getLogger(MultipartUploadResource.class);

    private final StreamStore streamStore;
    private final BlockStore blockStore;
    private final AccessControl accessControl;
    private final ResourceProtection resourceProtection;
    private final Formatters formatters;

    @Inject
    public MultipartUploadResource(StreamStore streamStore, BlockStore blockStore, AccessControl accessControl, ResourceProtection resourceProtection, Formatters formatters) {
        this.streamStore = streamStore;
        this.blockStore = blockStore;
        this.accessControl = accessControl;
        this.resourceProtection = resourceProtection;
        this.formatters = formatters;
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public Response post(@Context HttpServletRequest request,
                         @FormDataParam("file") final InputStream uploadedInputStream,
                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws IOException, URISyntaxException {
        User user = resourceProtection.getUser(request);

        MetaData metaData = streamStore.writeStream(uploadedInputStream);
        metaData.putMetaData(MetaData.CREATED, formatters.formatDateTime(DateTime.now()));
        metaData.putMetaData(MetaData.NAME, contentDispositionHeader.getFileName());

        Hash uploadMetaDataHash = blockStore.writeMeta(metaData);

        Hash rootIdentifier = accessControl.getRootMetaBlockFor(user);
        if(rootIdentifier == null) {
            //Create a new root metadatablock by using this uploaded metablock
            accessControl.setRootMetaBlockFor(user, uploadMetaDataHash);
        } else {
            MetaData rootMetaData = blockStore.readMeta(rootIdentifier);
            rootMetaData.addMetaBlock(uploadMetaDataHash);
            rootIdentifier = blockStore.writeMeta(rootMetaData);
            accessControl.setRootMetaBlockFor(user, rootIdentifier);
        }

        //Post redirect
        return Response.status(Response.Status.MOVED_PERMANENTLY).header("Location", "/").build();
    }

}
