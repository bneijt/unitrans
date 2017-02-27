package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.User;
import nl.bneijt.unitrans.session.SessionService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Path("user")
public class UserResource {
    final static Logger LOGGER = getLogger(UserResource.class);

    private final MetadataService metadataService;

    @Inject
    public UserResource(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @POST
    @Path("new")
    public Response newUser(
            @FormParam("username") String username,
            @FormParam("password") String password) throws IOException, URISyntaxException {
        LOGGER.info("Create for user {}", username);
        //TODO only allow from localhost
        Optional<User> userOptional = metadataService.addUser(username, password);
        if(userOptional.isPresent()) {
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(userOptional.get()).build();
        }
        return JsonResponse.conflict("Already exists");
    }


}
