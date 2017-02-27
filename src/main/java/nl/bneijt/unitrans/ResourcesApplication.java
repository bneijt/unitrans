package nl.bneijt.unitrans;

import nl.bneijt.unitrans.resources.*;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.inject.Inject;

public class ResourcesApplication extends ResourceConfig {

    @Inject
    public ResourcesApplication(DataResource dataResource,
                                MetaDataResource metaDataResource,
                                SessionResource sessionResource,
                                UserResource userResource
                                ) {
        super();
        property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        property(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, true);
        property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
        property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        property(ServerProperties.TRACING, "OFF");
        //TODO Override State to see if we can get rid of more jersey garbage

        //Features
        register(JacksonFeature.class);
        register(MultiPartFeature.class);

        //Resources
        register(ManifestResource.class);

        //injected resources
        register(userResource);
        register(dataResource);
        register(metaDataResource);
        register(sessionResource);
    }


    @Override
    public String getApplicationName() {
        return this.getClass().getSimpleName();
    }
}
