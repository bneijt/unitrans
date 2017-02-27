package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.User;
import nl.bneijt.unitrans.session.SessionService;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URISyntaxException;

@Path("user")
public class UserResource {
    private final MetadataService metadataService;

    @Inject
    public UserResource(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @POST
    @Path("new")
    @Produces(MediaType.APPLICATION_JSON)
    public User get(
            @FormParam("username") String username,
            @FormParam("password") String password) throws IOException, URISyntaxException {
        //TODO only allow from localhost
        return metadataService.addUser(username, password);
    }


}
