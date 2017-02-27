package nl.bneijt.unitrans.metadata;


import com.google.common.collect.Lists;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.calls;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class Neo4JStorageTest {


    @Test
    public void shouldOpenAndClose() throws Exception {
        File testStorageLocation = File.createTempFile("testStorage", "neo");
        assertThat(testStorageLocation.delete(), is(true));

        Neo4JStorage neo4JStorage = new Neo4JStorage(testStorageLocation);

        assertThat(testStorageLocation.exists(), is(false));
        neo4JStorage.open();
        assertThat(testStorageLocation.exists(), is(true));
        neo4JStorage.close();
        assertThat(testStorageLocation.exists(), is(true));
        FileUtils.deleteDirectory(testStorageLocation);
    }


    @Test
    public void shouldSetPropertiesOnNode() throws Exception {
        UUID ident = UUID.randomUUID();
        UUID metaid = UUID.randomUUID();
        MetadataBlock mdb = new MetadataBlock(ident, Arrays.asList(metaid), Arrays.asList("murmurhash"));
        Node node = mock(Node.class);
        Neo4JStorage.copyProperties(mdb, node);
        verify(node).setProperty("ident", ident.toString());
        verify(node).setProperty("metas", new String[]{metaid.toString()});
        verify(node).setProperty("datas", new String[]{"murmurhash"});
    }

}