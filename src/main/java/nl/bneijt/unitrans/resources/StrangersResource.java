package nl.bneijt.unitrans.resources;

import nl.bneijt.unitrans.accesscontrol.Strangers;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/strangers.json")
public class StrangersResource {

    private final Strangers strangers;

    @Inject
    public StrangersResource(Strangers strangers) {
        this.strangers = strangers;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> get() throws IOException {
        return strangers.getStrangerIds();
    }
}
