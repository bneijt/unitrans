package nl.bneijt.unitrans.blockstore;

import com.google.common.collect.Streams;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.inject.Inject;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import org.slf4j.Logger;

import java.io.*;

import static org.slf4j.LoggerFactory.getLogger;


public class BlockService {
    final static Logger LOGGER = getLogger(BlockService.class);

    final HashDirectoryTree hashDirectoryTree;

    @Inject
    public BlockService(HashDirectoryTree hashDirectoryTree) {
        this.hashDirectoryTree = hashDirectoryTree;
    }


    /** Write data from inputStream as new data block to given Metadatablock
     *
     * Return a new Metadatablock pointing to the new data block
     * @param inputStream
     * @return
     * @throws IOException
     */
    public MetadataBlock appendData(MetadataBlock targetMetaBlock, InputStream inputStream) throws IOException {
        HashFunction hf = dataHashFunction();
        HashingInputStream hashingInputStream = new HashingInputStream(hf, inputStream);
        File tempFile = File.createTempFile("incoming", ".blk");
        try (FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {
            ByteStreams.copy(hashingInputStream, tempFileOutputStream);
            tempFileOutputStream.flush();
        }
        hashingInputStream.close();
        HashCode hash = hashingInputStream.hash();

        File blockLocation = hashDirectoryTree.locationFor(hash.toString());
        blockLocation.getParentFile().mkdirs();
        LOGGER.info("Adding data block at {}", blockLocation);
        Files.move(tempFile, blockLocation);

        MetadataBlock newMeta = new MetadataBlock(targetMetaBlock);
        newMeta.datas.add(hash.toString());
        return newMeta;
    }

    static public HashFunction dataHashFunction() {
        return Hashing.sha1();
    }

    public InputStream open(String datablockId) throws FileNotFoundException {
        return new FileInputStream(hashDirectoryTree.locationFor(datablockId));
    }
}
