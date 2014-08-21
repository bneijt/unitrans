package nl.bneijt.unitrans.blockstore;

import com.google.common.collect.Range;

import java.io.IOException;
import java.io.InputStream;

/**
 * Block level interface to a block store
 * <p/>
 * This is the lower-level part of StreamStore, knowing only about single block streams
 */
public interface BlockStore {
    /**
     * Open the given identifier as block
     *
     * @param identifier
     * @return An InputStream pointing to the given block, null if not found.
     */
    InputStream openBlock(Hash identifier) throws IOException;

    /**
     * Store the given input stream as a single block
     * Write the given inputstream to the block store and return an identifier to the block
     *
     * @param inputStream
     * @return An hash identifying the block
     * @throws IOException when reading or writing fails.
     */
    Hash writeBlock(InputStream inputStream) throws IOException;

    /**
     * Return the block size Range
     *
     * @return A range for the block size in the storage
     */
    Range<Long> blockSize();

    /**
     * Read the given metadata or return null if not found
     *
     * @param identifier
     * @return The metadata content or null if the identifier is not found.
     * @throws IOException when reading fails
     */
    MetaData readMeta(Hash identifier) throws IOException;

    /**
     * Write the given metadata to the blockstore
     *
     * @param metaData
     * @return The identifier the metadata is stored under.
     * @throws IOException
     */
    Hash writeMeta(MetaData metaData) throws IOException;
}
