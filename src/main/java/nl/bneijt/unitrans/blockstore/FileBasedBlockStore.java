package nl.bneijt.unitrans.blockstore;

import com.google.common.collect.Range;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;

/**
 * Block storage backed by a filesystem
 * There is no security on the blockstore, each access should first check with AccessControl
 */
@Singleton
public class FileBasedBlockStore implements BlockStore {

    private final FileBasedBlockStoreLocation location;
    private Logger logger = LoggerFactory.getLogger(FileBasedBlockStore.class);

    @Inject
    public FileBasedBlockStore(FileBasedBlockStoreLocation fileBasedBlockStoreLocation) {
        this.location = fileBasedBlockStoreLocation;
    }

    @Override
    public InputStream openBlock(Hash identifier) throws IOException {
        synchronized (location) {
            try {
                File blockLocation = location.blockLocation(identifier, BlockExtension.DEFAULT);
                return new FileInputStream(blockLocation);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
    }

    @Override
    public Hash writeBlock(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile("incoming", ".blk");
        Hash blockHash = copyWithHash(inputStream, tempFile);
        synchronized (location) {
            File blockFinalLocation = location.prepareForWriting(blockHash, BlockExtension.DEFAULT);
            if (!blockFinalLocation.exists()) {
                FileUtils.moveFile(tempFile, blockFinalLocation);
            } else {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    logger.error("Deletion of {} failed", tempFile);
                }
            }
        }
        return blockHash;
    }

    private Hash copyWithHash(InputStream inputStream, File tempFile) throws IOException {
        try (FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, Digest.defaultMessageDigest());
            long copiedLength = ByteStreams.copy(digestInputStream, tempFileOutputStream);
            if (copiedLength == 0) {
                return null;
            }
            Hash blockHash = new Hash(digestInputStream.getMessageDigest().digest());
            tempFileOutputStream.close();
            return blockHash;
        }
    }

    @Override
    public Range<Long> blockSize() {
        return Range.closed(1l, 1024 * 1024 * 1024l);
    }

    @Override
    public MetaData readMeta(Hash identifier) throws IOException {
        File metaDataFile = location.blockLocation(identifier, BlockExtension.META);
        FileInputStream metaDataInputStream = new FileInputStream(metaDataFile);
        return MetaDataSerializer.readFrom(metaDataInputStream);
    }

    @Override
    public Hash writeMeta(MetaData metaData) throws IOException {
        File tempFile = File.createTempFile("metadata", ".blk");
        Hash blockHash = copyMetaWithHash(metaData, tempFile);

        synchronized (location) {
            File blockFinalLocation = location.prepareForWriting(blockHash, BlockExtension.META);
            if (!blockFinalLocation.exists()) {
                FileUtils.moveFile(tempFile, blockFinalLocation);
            } else {
                tempFile.delete();
            }
        }
        return blockHash;
    }


    private Hash copyMetaWithHash(MetaData metaData, File tempFile) throws IOException {
        try (FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile)) {
            DigestOutputStream digestOutputStream = new DigestOutputStream(tempFileOutputStream, Digest.defaultMessageDigest());

            MetaDataSerializer.writeTo(metaData, digestOutputStream);
            Hash blockHash = new Hash(digestOutputStream.getMessageDigest().digest());
            tempFileOutputStream.close();
            return blockHash;
        }
    }
}
