package nl.bneijt.unitrans;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApplicationIntegrationTest {

    private static Application application;
    private static CommandlineConfiguration commandlineConfiguration;

    private static String baseUrl = null;

    @BeforeClass
    public static void startServer() throws Exception {
        ArgumentParser parser = Application.argumentParser();
        Namespace res = parser.parseArgs(new String[]{"--meta", "/tmp/untrans_test_meta", "--data", "/tmp/untrans_test_data"});
        commandlineConfiguration = new CommandlineConfiguration(res);

        Injector injector = Guice.createInjector(new UnitransModule(commandlineConfiguration));

        application = injector.getInstance(Application.class);
        application.startServer();
        baseUrl = "https://127.0.0.1:" + commandlineConfiguration.getServerPort() + "/";





        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(commandlineConfiguration.getKeyStoreLocation(), commandlineConfiguration.getKeyStorePassword().toCharArray(), new TrustSelfSignedStrategy())
                .build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        Unirest.setHttpClient(httpclient);


    }

    @AfterClass
    public static void stopServer() throws Exception {
        application.stopServer();
        FileUtils.deleteDirectory(commandlineConfiguration.getMetaStoreLocation());

    }


    @Test
    public void shouldBeConnectable() throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", commandlineConfiguration.getServerPort()));
            assertThat(socket.isConnected(), is(true));
            socket.close();
        }
    }


    @Test
    public void shouldCreateNewUser() throws Exception {
        HttpResponse<JsonNode> response = Unirest.post(baseUrl + "api/user/new")
                .queryString("username", "Mark")
                .field("username", "test")
                .field("password", "test")
                .asJson();

        assertThat(response.getStatus(), is(200));
        JSONObject jsonObject = response.getBody().getObject();
        assertThat(jsonObject.get("username"), is("test"));
        assertThat("no root blocks exists yet", jsonObject.getJSONArray("rootMetadataBlocks").length(), is(0));

        //Creating a new session should create a first block
        

    }


}
