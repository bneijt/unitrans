package nl.bneijt.unitrans.blockstore;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.StringStartsWith;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.MatcherAssert.assertThat;

public class FileBasedBlockStoreTest {

    class TemporaryFileBlockStore {

        public final BlockStore blockStore;
        private final File tempDir;

        TemporaryFileBlockStore() throws IOException {
            this.tempDir = Files.createTempDir();
            FileBasedBlockStoreLocation fileBasedBlockStoreLocation = new FileBasedBlockStoreLocation(tempDir);
            fileBasedBlockStoreLocation.prepare();
            blockStore = new FileBasedBlockStore(fileBasedBlockStoreLocation);
        }

        public void destroy() throws IOException {
            FileUtils.deleteDirectory(tempDir);
        }
    }

    @Test
    public void readingABlockAfterWritingShouldReturnTheSameBlock() throws Exception {
        TemporaryFileBlockStore temporaryFileBlockStore = new TemporaryFileBlockStore();
        try {
            BlockStore blockStore = temporaryFileBlockStore.blockStore;

            String testString = "This is a test";
            ByteArrayInputStream stringInputStream = new ByteArrayInputStream(testString.getBytes());

            Hash hash = blockStore.writeBlock(stringInputStream);
            assertThat(hash.toBase16(), StringStartsWith.startsWith("A028D4F74B602BA45EB0A93C9A4"));

            //Read the block back to see if it's there
            InputStream inputStreamFromBlockstore = blockStore.openBlock(hash);
            assertThat(inputStreamFromBlockstore, IsNull.notNullValue());
            String stringReadFromBlockstore = IOUtils.toString(inputStreamFromBlockstore);

            assertThat(stringReadFromBlockstore, Is.is(testString));
        } finally {
            temporaryFileBlockStore.destroy();
        }

    }

    @Test
    public void randomBlockShouldNotBeInEmptyStorage() throws Exception {
        TemporaryFileBlockStore temporaryFileBlockStore = new TemporaryFileBlockStore();
        try {
            Hash identifier = HashTest.randomHash();
            InputStream inputStream = temporaryFileBlockStore.blockStore.openBlock(identifier);
            assertThat(inputStream, IsNull.nullValue());
        } finally {
            temporaryFileBlockStore.destroy();
        }
    }

    @Test
    public void missingMetadataShouldThrowIOException() throws Exception {
        TemporaryFileBlockStore temporaryFileBlockStore = new TemporaryFileBlockStore();
        try {
            BlockStore blockStore = temporaryFileBlockStore.blockStore;
            String exceptionMessage = "";
            try {
                blockStore.readMeta(HashTest.randomHash());
            } catch (IOException e) {
                exceptionMessage = e.getMessage();
            }
            assertThat(exceptionMessage.toLowerCase(), StringContains.containsString("no such file"));
        } finally {
            temporaryFileBlockStore.destroy();
        }
    }

    @Test
    public void shouldAllowForEmptyblockToBeWritten() throws IOException {
        TemporaryFileBlockStore temporaryFileBlockStore = new TemporaryFileBlockStore();
        try {
            BlockStore blockStore = temporaryFileBlockStore.blockStore;

            MetaData emptyRootMetadata = new MetaData();

            Hash identifier = blockStore.writeMeta(emptyRootMetadata);
            MetaData readMetaData = blockStore.readMeta(identifier);

            assertThat(readMetaData.getBlockHashList().size(), Is.is(0));
            assertThat(readMetaData.getMetaData().size(), Is.is(0));
        } finally {
            temporaryFileBlockStore.destroy();
        }
    }

    @Test
    public void metadataShouldBeRecoverable() throws Exception {
        TemporaryFileBlockStore temporaryFileBlockStore = new TemporaryFileBlockStore();
        try {
            BlockStore blockStore = temporaryFileBlockStore.blockStore;
            MetaData metaData = new MetaData();
            Hash storedReference = HashTest.randomHash();
            metaData.putMetaData("hello", "this is a test");
            metaData.addBlock(storedReference);


            Hash identifier = blockStore.writeMeta(metaData);
            MetaData readMetaData = blockStore.readMeta(identifier);

            assertThat(readMetaData.getBlockHashList().get(0).toBase16(), Is.is(storedReference.toBase16()));
            assertThat(readMetaData.getMetaData().get("hello"), Is.is("this is a test"));
        } finally {
            temporaryFileBlockStore.destroy();
        }
    }

}
