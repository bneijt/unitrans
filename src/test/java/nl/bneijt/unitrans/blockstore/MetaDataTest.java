package nl.bneijt.unitrans.blockstore;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.Collections;

public class MetaDataTest {
    @Test
    public void shouldUseMutableCollectionsAfterConstruction() throws Exception {
        MetaData metaData = new MetaData(Collections.<String, String>emptyMap(), Collections.<Hash>emptyList(), Collections.<Hash>emptyList());
        metaData.addBlock(HashTest.randomHash());
    }
}
