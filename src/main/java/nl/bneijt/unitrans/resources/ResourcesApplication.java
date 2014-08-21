package nl.bneijt.unitrans.resources;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import javax.inject.Inject;

public class ResourcesApplication extends ResourceConfig {

    @Inject
    public ResourcesApplication(BlockResource blockResource,
                                MetaDataResource metaDataResource,
                                MultipartUploadResource multipartUploadResource,
                                StrangersResource strangersResource,
                                DownloadResource downloadResource) {
        super();
        super.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);
        super.property(CommonProperties.JSON_PROCESSING_FEATURE_DISABLE, true);
        super.property(CommonProperties.MOXY_JSON_FEATURE_DISABLE, true);
        super.property(ServerProperties.METAINF_SERVICES_LOOKUP_DISABLE, true);
        super.property(ServerProperties.TRACING, "OFF");
        //TODO Override State to see if we can get rid of more jersey garbage

        //Features
        super.register(JacksonFeature.class);
        super.register(MultiPartFeature.class);

        //Resources
        super.register(ManifestResource.class);
        super.register(RequestAttributesResource.class);

        //getSingletons
        super.register(blockResource);
        super.register(metaDataResource);
        super.register(multipartUploadResource);
        super.register(strangersResource);
        super.register(downloadResource);
    }


    @Override
    public String getApplicationName() {
        return this.getClass().getSimpleName();
    }


}
