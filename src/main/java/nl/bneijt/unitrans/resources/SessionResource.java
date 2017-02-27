package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.metadata.MetadataService;
import nl.bneijt.unitrans.metadata.elements.User;
import nl.bneijt.unitrans.session.SessionService;
import nl.bneijt.unitrans.session.elements.Session;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

@Path("session")
public class SessionResource {


    private final SessionService sessionService;
    private final MetadataService metadataService;

    @Inject
    public SessionResource(SessionService sessionService, MetadataService metadataService) {
        this.sessionService = sessionService;
        this.metadataService = metadataService;
    }

    @GET
    @Path("{sessionId}")
    public Response get(@PathParam("sessionId") String sessionId) throws IOException, URISyntaxException {
        UUID sessionIdent = UUID.fromString(sessionId);
        Optional<Session> sessionOptional = sessionService.get(sessionIdent);
        if (sessionOptional.isPresent()) {
            Session session = sessionOptional.get();
            return Response.temporaryRedirect(new URI("../meta/" + sessionId + "/" + session.rootBlock.toString())).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("No session found").build();
    }

    @POST
    @Path("new")
    public Response login(@FormParam("username") String username,
                          @FormParam("password") String password) throws IOException, URISyntaxException {

        User user = metadataService.getUser(username, password);

        Session session = sessionService.open(user.username, user.rootMetadataBlocks.get(0));

        return Response.temporaryRedirect(new URI(session.ident.toString())).build();
    }


}
