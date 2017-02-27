package nl.bneijt.unitrans.metadata;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.metadata.elements.User;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class MetadataService implements Closeable {
final static Logger LOGGER = getLogger(MetadataService.class);


    private final Neo4JStorage neo4JStorage;

    @Inject
    public MetadataService(Neo4JStorage neo4JStorage) {
        this.neo4JStorage = neo4JStorage;
        this.neo4JStorage.open();
        //TODO move to application layer lifecycle
    }




    synchronized public boolean reachableFrom(UUID rootBlock, UUID metaIdent) {
        //TODO
        return true;
    }

    public MetadataBlock appendMetadata(MetadataBlock targetBlock, MetadataBlock blockToAppend) {
        //Create new block
        MetadataBlock newBlock = new MetadataBlock(targetBlock);
        newBlock.metas.add(blockToAppend.ident);
        return newBlock;
    }

    public void write(List<MetadataBlock> blocks) {
        for (MetadataBlock block : blocks) {
            neo4JStorage.createOrIgnore(block);
        }
    }

    public MetadataBlock get(UUID metaIdent) {
        return neo4JStorage.get(metaIdent).get();
    }

    /** Generate a new root path given that targetBlock should be replaced by newTargetBlock
     *
     *
     * @param username
     * @param targetBlock
     * @param newTargetBlock
     * @return the uuid of the new root
     */
    public UUID reRoot(String username, MetadataBlock targetBlock, MetadataBlock newTargetBlock) {
        //Find all references to targetBlock and update them to point to newTargetBlock
        //For all those references, do the same??

        return UUID.randomUUID();
    }

    /** Get user information, or fail if the password is incorrect
     *
     * @param username
     * @param password
     * @return user if the user exists and the password is correct
     */
    public Optional<User> getUser(String username, String password) {
        return neo4JStorage.getUser(username, password);
       }

    /**
     * Add the user with the given username and password.
     * @param username
     * @param password
     * @return The new user or empty if the user already existed
     */
    public Optional<User> addUser(String username, String password) {
        //Check if the user already exists
        if(neo4JStorage.hasUser(username)) {
            return Optional.empty();
        };



        MetadataBlock rootBlock = MetadataBlock.emptyRandomBlock();
        neo4JStorage.createOrIgnore(rootBlock);
        return neo4JStorage.addUser(username, password, rootBlock);
    }


    @Override
    public void close() throws IOException {
        neo4JStorage.close();
    }
}
