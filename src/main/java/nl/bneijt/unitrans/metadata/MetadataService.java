package nl.bneijt.unitrans.metadata;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.metadata.elements.User;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

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
        if (rootBlock.equals(metaIdent)) {
            return true;
        }
        List<List<UUID>> paths = neo4JStorage.pathsFromTo(rootBlock, metaIdent);
        return paths.size() > 0;
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

    /**
     * Generate a new root path given that targetBlock should be replaced by newTargetBlock
     *
     * @param rootBlockIdent
     * @param targetBlock
     * @param newTargetBlock
     * @return the new blocks required to create the new path, including the newly elected root block as last element of the list
     */
    public List<MetadataBlock> reRoot(UUID rootBlockIdent, MetadataBlock targetBlock, MetadataBlock newTargetBlock) {
        //Find all references to targetBlock and create new blocks restoring the original path to the user's root
        // block
        List<List<UUID>> pathsFromRootToOldBlock = neo4JStorage.pathsFromTo(rootBlockIdent, targetBlock.ident);

        //Mutable list of blocks which are the new versions of old blocks
        //must be a linked hashmap to preserve insertion ordering
        LinkedHashMap<UUID, MetadataBlock> newBlockMap = new LinkedHashMap<>();

        //Rootblock is the first targetblock new block reference
        newBlockMap.put(targetBlock.ident, newTargetBlock);

        for (List<UUID> possiblePath : pathsFromRootToOldBlock) {

            FluentIterable.from(Lists.reverse(possiblePath))
                .forEach(uuid -> {
                        if (!newBlockMap.containsKey(uuid)) {
                            MetadataBlock oldBlock = get(uuid);
                            MetadataBlock newBlock = new MetadataBlock(oldBlock);
                            //If the new block references a block in the new blockset
                            // update the reference to point to the new block
                            newBlock = newBlock.replaceMetas(FluentIterable.from(oldBlock.metas).transform(oldReference -> {
                                if (newBlockMap.containsKey(oldReference)) {
                                    return newBlockMap.get(oldReference).ident;
                                }
                                return oldReference;
                            }).toList());
                            newBlockMap.put(uuid, newBlock);
                        } else {
                            //The block has already been registered, so it must contain a second path
                            //to the same block
                            throw new RuntimeException("Multiple paths from the same block??? Still not implemented");
                        }
                    }
                );

        }

        //For all those references, do the same??
        //LAST BLOCK MUST BE THE NEW ROOT, which is why we use a linked
        return Lists.reverse(new ArrayList<>(newBlockMap.values()));
    }

    /**
     * Get user information, or fail if the password is incorrect
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
     *
     * @param username
     * @param password
     * @return The new user or empty if the user already existed
     */
    public Optional<User> addUser(String username, String password) {
        //Check if the user already exists
        if (neo4JStorage.hasUser(username)) {
            return Optional.empty();
        }
        ;


        MetadataBlock rootBlock = MetadataBlock.emptyRandomBlock();
        neo4JStorage.createOrIgnore(rootBlock);
        return neo4JStorage.addUser(username, password, rootBlock);
    }


    @Override
    public void close() throws IOException {
        neo4JStorage.close();
    }
}
