package nl.bneijt.unitrans;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONObject;
import org.neo4j.helpers.collection.Iterables;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/** Statefull client api implementation for unit testing
 * Will ignore certificates etc
 */
public class TestClient {
    private final String baseUrl;
    public UUID rootBlock;
    public UUID session;

    public TestClient(CommandlineConfiguration commandlineConfiguration) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {

        this.baseUrl = "https://127.0.0.1:" + commandlineConfiguration.getServerPort() + "/";
        ;
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

    public HttpResponse<JsonNode> getJson(String apiPath) throws UnirestException {
        return Unirest.get(baseUrl + "api/" + apiPath).asJson();
    }

    public UUID createUser(String username, String password) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post(baseUrl + "api/user/new")
                .field("username", username)
                .field("password", password)
                .asJson();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        JSONObject jsonObject = response.getBody().getObject();
        assertThat(jsonObject.get("username"), is(username));
        assertThat("Should automatically get a new root block", jsonObject.getJSONArray("rootMetadataBlocks").length(), is(1));
        String rootBlockIdent = jsonObject.getJSONArray("rootMetadataBlocks").getString(0);
        return UUID.fromString(rootBlockIdent);
    }

    public HttpResponse<JsonNode> login(String username, String password) throws UnirestException {
        //Creating a new session should create a first block
        HttpResponse<JsonNode> response = Unirest.post(baseUrl + "api/session/new")
            .field("username", username)
            .field("password", password)
            .asJson();

        if(response.getStatus() != Response.Status.SEE_OTHER.getStatusCode()) {
            return response;
        }

        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));

        String newSessionLocation = response.getHeaders().get("Location").get(0);
        assertThat(newSessionLocation,  containsString("api/session"));


        //Follow session to root block
        HttpResponse<String> sessionRedirect = Unirest.get(newSessionLocation).asString();
        assertThat(sessionRedirect.getStatus(), is(Response.Status.TEMPORARY_REDIRECT.getStatusCode()));

        String rootBlockForSessionLocation = sessionRedirect.getHeaders().get("Location").get(0);

        String[] split = rootBlockForSessionLocation.split("/");

        //Last two elements should be an UUID
        UUID.fromString(split[split.length -1]);
        session = UUID.fromString(split[split.length - 2]);

        HttpResponse<JsonNode> rootBlockResponse = Unirest.get(rootBlockForSessionLocation).asJson();
        assertThat(rootBlockResponse.getStatus(), is(Response.Status.OK.getStatusCode()));

        JSONObject metaBlock = rootBlockResponse.getBody().getObject();
        rootBlock = UUID.fromString(metaBlock.getString("ident"));
        assertThat("Should not have any other files yet", metaBlock.getJSONArray("metas").length(), is(0));
        assertThat("Should have no data connected yet", metaBlock.getJSONArray("datas").length(), is(0));
        return response;
    }

    /** Append a metadata block to a given block
     * @param targetBlock
     * @return       The id of the new block
     * @throws UnirestException
     */
    public UUID appendMetaBlock(UUID targetBlock) throws UnirestException {

            HttpResponse<JsonNode> response = Unirest
                .post(baseUrl + "api/meta/" + session.toString() + "/" + targetBlock.toString() + "/append")
                .body("{}")
                .asJson();

            //The response is a redirect to the newly created block
            assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
            String newMetadataBlockLocation = response.getHeaders().get("Location").get(0);
            List<String> split = Arrays.asList(newMetadataBlockLocation.split("/"));

            String sameSession = Iterables.fromEnd(split, 1);

            assertThat("Same session is valid", sameSession, is(session.toString()));
            return UUID.fromString(Iterables.last(split)); //New meta block
    }

    public UUID appendDataString(UUID targetBlock, String value) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest
            .post(baseUrl + "api/data/" + session.toString() + "/" + targetBlock.toString())
            .body(value)
            .asJson();
        if(response.getStatus() != Response.Status.SEE_OTHER.getStatusCode()) {
            throw new RuntimeException("Failed to upload data");
        }
        //The response is a redirect to the newly updated metablock
        assertThat(response.getStatus(), is(Response.Status.SEE_OTHER.getStatusCode()));
        String newMetadataBlockLocation = response.getHeaders().get("Location").get(0);
        List<String> split = Arrays.asList(newMetadataBlockLocation.split("/"));

        String newBlock = Iterables.last(split);
        String sameSession = Iterables.fromEnd(split, 1);
        assertThat(sameSession, is(session.toString()));
        return UUID.fromString(newBlock);

    }

    public String readDataString(UUID metadataBlock, String dataBlockHash) throws UnirestException, IOException {
        HttpResponse<InputStream> response = Unirest
            .get(baseUrl + "api/data/" + session.toString()
                + "/" + metadataBlock.toString()
                + "/" + dataBlockHash.toString()
            ).asBinary();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        return CharStreams.toString(new InputStreamReader(response.getBody(), Charsets.UTF_8));


    }
}
