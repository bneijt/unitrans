package nl.bneijt.unitrans.blockstore;

import java.io.IOException;
import java.io.InputStream;

/** A high-level interface to a blockstore using streams.
 *
 * This will read and write complete files.
 */
public interface StreamStore {
    /** Store the given input stream as a collection of blocks, returning a referencing MetaData structure.
     * There is no guarantee that the MetaData block will not contain references to other MetaData blocks.
     * Most implementations will use storeblock
     * @param inputStream
     * @return An hash identifying the block
     * @throws IOException when reading or writing fails.
     */
    MetaData writeStream(InputStream inputStream) throws IOException;


    /** Read all data blocks under the given toplevel metadata block
     *
     * @param root The top level metadata block
     * @return An inputstream that concatenates all data blocks under root using detph first
     * @throws IOException
     */
    InputStream readStream(MetaData root) throws IOException;
}
