package nl.bneijt.unitrans.metadata;

import com.google.common.io.Files;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class MetadataServiceTest {

    @Test
    public void shouldFindPathBetweenConnectedBlocks() throws Exception {
        MatcherAssert.assertThat(true, is(true));

        File testStorageLocation = Files.createTempDir();

        MetadataService service = new MetadataService(new Neo4JStorage(testStorageLocation));


        MetadataBlock rootBlock = MetadataBlock.emptyRandomBlock();
        MetadataBlock firstBlock = MetadataBlock.emptyRandomBlock();

        rootBlock.metas.add(firstBlock.ident);

        service.write(Arrays.asList(firstBlock, rootBlock));
        assertThat(service.reachableFrom(rootBlock.ident, firstBlock.ident), is(true));


        MetadataBlock a = MetadataBlock.emptyRandomBlock();
        MetadataBlock b = MetadataBlock.emptyRandomBlock();
        MetadataBlock c = MetadataBlock.emptyRandomBlock();

        a.metas.add(b.ident);
        b.metas.add(c.ident);

        service.write(Arrays.asList(c, b, a));
        //Forward
        assertThat(service.reachableFrom(a.ident, c.ident), is(true));
        assertThat(service.reachableFrom(b.ident, c.ident), is(true));

        //Backward
        assertThat(service.reachableFrom(c.ident, a.ident), is(false));
        assertThat(service.reachableFrom(b.ident, a.ident), is(false));

        service.close();

        FileUtils.deleteDirectory(testStorageLocation);

    }




}