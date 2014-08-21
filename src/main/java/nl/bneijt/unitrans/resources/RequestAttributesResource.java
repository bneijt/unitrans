package nl.bneijt.unitrans.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Path("/requestAttributes.json")
public class RequestAttributesResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> get(@Context HttpServletRequest request) throws IOException {
        Enumeration<String> attributeNames = request.getAttributeNames();
        HashMap hashMap = new HashMap();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            hashMap.put(attributeName, request.getAttribute(attributeName).toString());
        }
        return hashMap;
    }
}
