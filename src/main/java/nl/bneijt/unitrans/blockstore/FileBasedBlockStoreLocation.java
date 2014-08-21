package nl.bneijt.unitrans.blockstore;

import com.google.common.base.Splitter;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Defines the locations for the files to be stored
 */
public class FileBasedBlockStoreLocation {
    private final Logger logger = LoggerFactory.getLogger(FileBasedBlockStoreLocation.class);

    private final File blockBasePath;
    private final Splitter identifierSplitter;

    public FileBasedBlockStoreLocation(File basePath) {
        if (!basePath.isDirectory()) {
            logger.error("Basepath for storage location does not exist, could not find {}", basePath);
        }
        this.blockBasePath = new File(basePath, "blocks");
        this.identifierSplitter = Splitter.fixedLength(2).limit(2);
    }


    /**
     * Return the File for a given block hash
     *
     * @param hash
     * @return
     */
    public File blockLocation(Hash hash, BlockExtension blockExtension) {
        String identifier = hash.toBase16();
        Iterable<String> identifierElements = identifierSplitter.split(identifier + blockExtension.extension);
        File location = blockBasePath;
        for (String identifierElement : identifierElements) {
            location = new File(location, identifierElement);
        }

        return location;
    }

    /**
     * Get the File to a block hash location which can be written to.
     * A utility method to make sure parent directories exist if needed
     *
     * @param hash identifying the block
     * @return An OutputStream to the given location
     * @throws IOException
     */
    public File prepareForWriting(Hash hash, BlockExtension extension) throws IOException {
        File location = blockLocation(hash, extension);
        Files.createParentDirs(location);
        return location;
    }


    /**
     * Makes the directories needed for this block storage to work.
     * This should be done at least once to have a better idea of the storage location working in the future.
     *
     * @throws FileNotFoundException
     */
    public void prepare() throws IOException {
        if (!blockBasePath.exists()) {
            Files.createParentDirs(blockBasePath);
        }
    }

}
