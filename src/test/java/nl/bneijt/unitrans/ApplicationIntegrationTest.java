package nl.bneijt.unitrans;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
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
import javax.ws.rs.core.Response;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
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

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        CloseableHttpClient httpclient = HttpClients.custom()
                .disableRedirectHandling()
                .setSSLSocketFactory(sslsf)
                .build();

        Unirest.setHttpClient(httpclient);


    }

    @AfterClass
    public static void stopServer() throws Exception {
        application.stopServer();
        FileUtils.deleteDirectory(commandlineConfiguration.getMetaStoreLocation());
        FileUtils.deleteDirectory(commandlineConfiguration.getDataStoreLocation());

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
    public void shouldHostManifestInformation() throws Exception {
        HttpResponse<JsonNode> response = Unirest.get(baseUrl + "api/manifest.json").asJson();
    }

    @Test
    public void shouldHandleUserLoginFlow() throws Exception {
        String testUsername = "test";
        String testPassword = "testpass";
        String rootblock = createUser(testUsername, testPassword);

        createSessionExpectingRootblock(testUsername, testPassword, rootblock);

    }

    private void createSessionExpectingRootblock(String username, String password, String rootBlockIdent) throws Exception {
        //Creating a new session should create a first block
        HttpResponse<String> response = Unirest.post(baseUrl + "api/session/new")
                .field("username", username)
                .field("password", password)
                .asString();
        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));

        String newSessionLocation = response.getHeaders().get("Location").get(0);
        assertThat(newSessionLocation,  containsString("api/session"));


        //Follow session to root block
        HttpResponse<String> sessionRedirect = Unirest.get(newSessionLocation).asString();
        assertThat(sessionRedirect.getStatus(), is(Response.Status.TEMPORARY_REDIRECT.getStatusCode()));

        String rootBlockForSessionLocation = sessionRedirect.getHeaders().get("Location").get(0);

        HttpResponse<JsonNode> rootBlock = Unirest.get(rootBlockForSessionLocation).asJson();

        assertThat(rootBlock.getStatus(), is(Response.Status.OK.getStatusCode()));

    }

    private String createUser(String username, String password) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post(baseUrl + "api/user/new")
                .field("username", username)
                .field("password", password)
                .asJson();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        JSONObject jsonObject = response.getBody().getObject();
        assertThat(jsonObject.get("username"), is("test"));
        assertThat("Should automatically get a new root block", jsonObject.getJSONArray("rootMetadataBlocks").length(), is(1));
        return jsonObject.getJSONArray("rootMetadataBlocks").getString(0);
    }


}
