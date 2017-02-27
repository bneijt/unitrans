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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class BlockService {

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
        HashFunction hf = Hashing.sha1();
        HashingInputStream hashingInputStream = new HashingInputStream(hf, inputStream);
        File tempFile = File.createTempFile("incoming", ".blk");
        try (FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {
            ByteStreams.copy(hashingInputStream, tempFileOutputStream);
        }
        hashingInputStream.close();
        HashCode hash = hashingInputStream.hash();

        File blockLocation = hashDirectoryTree.locationFor(hash);
        blockLocation.getParentFile().mkdirs();
        Files.move(tempFile, blockLocation);

        MetadataBlock newMeta = new MetadataBlock(targetMetaBlock);
        newMeta.datas.add(hash.toString());
        return newMeta;
    }
}
