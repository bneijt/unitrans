package nl.bneijt.unitrans.metadata;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.metadata.elements.User;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Singleton
public class MetadataService implements Closeable {
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

    public User getUser(String username, String password) {
        return neo4JStorage.getUser(username, password).get();
    }

    public User addUser(String username, String password) {
        return neo4JStorage.addUser(username, password).get();
    }


    @Override
    public void close() throws IOException {
        neo4JStorage.close();
    }
}
