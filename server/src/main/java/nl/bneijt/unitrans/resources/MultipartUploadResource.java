package nl.bneijt.unitrans.resources;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

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

/**
 * @deprecated Reintroduce later
 */
@Path("/upload/multipart")
public class MultipartUploadResource {

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public Response post(@Context HttpServletRequest request,
                         @FormDataParam("file") final InputStream uploadedInputStream,
                         @FormDataParam("file") FormDataContentDisposition contentDispositionHeader) throws IOException, URISyntaxException {
        //Post redirect
        return Response.status(Response.Status.MOVED_PERMANENTLY).header("Location", "/").build();
    }

}
