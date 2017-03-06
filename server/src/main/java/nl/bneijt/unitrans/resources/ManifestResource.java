package nl.bneijt.unitrans.resources;

import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

@Path("/manifest.json")
public class ManifestResource {
    private org.slf4j.Logger logger = LoggerFactory.getLogger(ManifestResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> get() throws IOException {
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (resources.hasMoreElements()) {
            try {
                URL manifestUrl = resources.nextElement();

                Manifest manifest = new Manifest(manifestUrl.openStream());
                HashMap<String, String> mainAttributes = new HashMap<>();
                for (Map.Entry<Object, Object> entry : manifest.getMainAttributes().entrySet()) {
                    mainAttributes.put(entry.getKey().toString(), entry.getValue().toString());
                }
                return mainAttributes;
            } catch (IOException e) {
                logger.error("Failed to load manifest", e);
            }
        }
        throw new IOException("Could not find any parsable manifest file on classpath");
    }
}
