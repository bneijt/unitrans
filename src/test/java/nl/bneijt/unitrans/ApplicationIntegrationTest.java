package nl.bneijt.unitrans;

import com.google.inject.Injector;
import nl.bneijt.unitrans.resources.ResourcesApplication;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ApplicationIntegrationTest {

    private static Server jettyServer;

    @BeforeClass
    public static void startServer() throws Exception {
        Injector injector = TestUnitransModule.createInjector();
        Application application = injector.getInstance(Application.class);
        jettyServer = application.createJettyServer(injector.getInstance(ResourcesApplication.class));
        jettyServer.start();

    }

    @AfterClass
    public static void stopServer() throws Exception {
        jettyServer.stop();
    }



    @Test
    @Ignore
    public void shouldAcceptBlocks() throws FileNotFoundException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8443").path("api/block/");
        FileInputStream fileInputStream = new FileInputStream(new File("/tmp/test_file"));

        target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .post(Entity.entity(fileInputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE));
//        HttpClient httpclient = new DefaultHttpClient();
//        HttpPost httppost = new HttpPost(url);
//        FileBody fileContent = new FileBody(new File(fileName));
//        StringBody comment = new StringBody("Filename: " + fileName);
//        MultipartEntity reqEntity = new MultipartEntity();
//        reqEntity.addPart("file", fileContent);
//        httppost.setEntity(reqEntity);
//        HttpResponse response = httpclient.execute(httppost);
//        HttpEntity resEntity = response.getEntity();

    }
}
