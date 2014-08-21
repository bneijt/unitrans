package nl.bneijt.unitrans.resources;

import com.google.inject.Singleton;
import nl.bneijt.unitrans.accesscontrol.AccessControl;
import nl.bneijt.unitrans.accesscontrol.User;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


@Singleton
public class ResourceProtection {
    public static final WebApplicationException NOT_AUTHORIZED = new WebApplicationException("Not authorized", Response.Status.FORBIDDEN);
    private final AccessControl accessControl;

    @Inject
    public ResourceProtection(AccessControl accessControl) {
        this.accessControl = accessControl;
    }

    public User getUser(ServletRequest request) throws WebApplicationException {
        Object maybeUser = request.getAttribute(User.class.getName());
        if (maybeUser == null) {
            throw NOT_AUTHORIZED;
        }
        if (maybeUser instanceof User) {
            return (User) maybeUser;
        }
        throw NOT_AUTHORIZED;
    }


    public void throwIfNotAllowedToWrite(User user) {
        if (accessControl.canWrite(user)) {
            return;
        }
        throw NOT_AUTHORIZED;
    }
}
