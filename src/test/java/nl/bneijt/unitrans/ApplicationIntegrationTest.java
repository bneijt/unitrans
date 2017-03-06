package nl.bneijt.unitrans;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.bneijt.unitrans.blockstore.BlockService;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApplicationIntegrationTest {

    private static Application application;
    private static CommandlineConfiguration commandlineConfiguration;

    @BeforeClass
    public static void startServer() throws Exception {
        ArgumentParser parser = Application.argumentParser();
        ;

        Namespace res = parser.parseArgs(new String[]{
                "--meta", Files.createTempDir().toString(),
                "--data", Files.createTempDir().toString()
        });
        commandlineConfiguration = new CommandlineConfiguration(res);

        Injector injector = Guice.createInjector(new UnitransModule(commandlineConfiguration));

        application = injector.getInstance(Application.class);
        application.startServer();

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
        TestClient client = new TestClient(commandlineConfiguration);

        HttpResponse<JsonNode> response = client.getJson("manifest.json");
        assertThat(response.getBody().getObject().has("Manifest-Version"), is(true));
    }


    @Test
    public void shouldHandleGreenFlow() throws Exception {
        String testUsername = "test";
        String testPassword = "testpass";
        TestClient client = new TestClient(commandlineConfiguration);

        UUID rootBlockForNewUser = client.createUser(testUsername, testPassword);
        client.login(testUsername, testPassword);

        assertThat("Should have the rootblock created for the user", client.rootBlock, is(rootBlockForNewUser));

        //Create simple root -> b -> c
        //Add one metablock
        UUID blockB = client.appendMetaBlock(rootBlockForNewUser);
        //Add yet another block
        UUID blockC = client.appendMetaBlock(blockB);
        String simpleText = "Simple text";
        HashCode simpleTextHashCode = BlockService.dataHashFunction().newHasher().putString(simpleText, Charsets.UTF_8).hash();

        UUID blockD = client.appendDataString(blockC, simpleText);
        String string = client.readDataString(blockD, simpleTextHashCode.toString());
        assertThat(string, is(simpleText));

        //Adding a second text to the same block

        String otherText = "Other text";
        HashCode otherTextHashCode = BlockService.dataHashFunction().newHasher().putString(otherText, Charsets.UTF_8).hash();
        UUID blockE = client.appendDataString(blockD, otherText);
        assertThat(client.readDataString(blockE, simpleTextHashCode.toString()), is(simpleText));
        assertThat(client.readDataString(blockE, otherTextHashCode.toString()), is(otherText));



    }


    @Test
    public void shouldFailToLogInWithWrongPassword() throws Exception {
        String username = "a";
        TestClient client = new TestClient(commandlineConfiguration);
        client.createUser(username, "realPassword");

        HttpResponse<JsonNode> response = client.login(username, "invalidPassword");
        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }


    @Test
    public void shouldFailToLogInWithWrongUsername() throws Exception {
        //Should fail to log in with invalid password
        TestClient client = new TestClient(commandlineConfiguration);
        HttpResponse<JsonNode> response = client.login("404", "anything");
        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
    }

}
