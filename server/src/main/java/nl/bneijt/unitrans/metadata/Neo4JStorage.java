package nl.bneijt.unitrans.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import nl.bneijt.unitrans.metadata.elements.MetadataBlock;
import nl.bneijt.unitrans.metadata.elements.User;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;

import java.io.File;
import java.util.*;

@Singleton
public class Neo4JStorage {

    private GraphDatabaseService graphDb;

    private Label userLabel = Label.label("user");
    private Label metadataLabel = Label.label("metadata");
    private final File basePath;


    public enum UnitransRelationshipTypes implements RelationshipType {
        CHILD_ELEMENT;
    }

    @Inject
    public Neo4JStorage(File basePath) {
        this.basePath = basePath;
    }


    public void open() {
        basePath.getParentFile().mkdirs();
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(basePath);

        //Create user schema
        IndexDefinition indexDefinition;
        try (Transaction tx = graphDb.beginTx()) {
            Schema schema = graphDb.schema();
            boolean userLabelIndexAvailable = schema.getIndexes(userLabel).iterator().hasNext();
            if (!userLabelIndexAvailable) {
                indexDefinition = schema.indexFor(userLabel)
                        .on("username")
                        .create();
            }
            boolean metaLabelIndexAvailable = schema.getIndexes(userLabel).iterator().hasNext();
            if (!metaLabelIndexAvailable) {
                indexDefinition = schema.indexFor(metadataLabel)
                        .on("ident")
                        .create();
            }

            tx.success();
        }
    }

    public Optional<User> addUser(String username, String plainPassword, MetadataBlock rootBlock) {
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = graphDb.createNode(userLabel);
            userNode.setProperty("username", username);
            userNode.setProperty("password", BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            userNode.setProperty("roots", new String[]{rootBlock.ident.toString()});
            tx.success();
        }
        return getUser(username, plainPassword);
    }

    public Optional<User> getUser(String username, String plainPassword) {
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = graphDb.findNode(userLabel, "username", username);
            tx.success();

            if (userNode == null) {
                return Optional.empty();
            }

            //Check password
            if (BCrypt.checkpw(plainPassword, (String) userNode.getProperty("password"))) {
                return Optional.of(new User(username, readMetasFrom(userNode, "roots")));
            } else {
                return Optional.empty();
            }
        }
    }

    public boolean hasUser(String username) {
        try (Transaction tx = graphDb.beginTx()) {
            Node userNode = graphDb.findNode(userLabel, "username", username);
            tx.success();
            return userNode != null;
        }
    }

    public Optional<MetadataBlock> get(UUID ident) {
        try (Transaction tx = graphDb.beginTx()) {
            Node metaNode = graphDb.findNode(metadataLabel, "ident", ident.toString());
            if (metaNode == null) {
                return Optional.empty();
            }
            HashSet<String> propertyKeys = Sets.newHashSet(metaNode.getPropertyKeys());

            tx.success();

            //Remove ident
            assert ident.toString().equals((String) metaNode.getProperty("ident"));
            propertyKeys.remove("ident");

            return Optional.of(new MetadataBlock(
                    ident,
                    readMetasFrom(metaNode, "metas"),
                    Arrays.asList((String[]) metaNode.getProperty("datas"))
            ));
        }

    }

    /**
     * List all paths from a to b
     * Current maximum depth is 1024 elements!
     *
     * @param a
     * @param b
     * @return list of possible paths from a to b
     */
    public List<List<UUID>> pathsFromTo(UUID a, UUID b) {
        List<List<UUID>> paths = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute("MATCH p=(a)-[e*1..1024]->(b)\n" +
                            "WHERE a.ident = {identA} AND b.ident = {identB}\n" +
                            "RETURN extract(n IN nodes(p) | n.ident) as path"
                    , ImmutableMap.of("identA", a.toString(), "identB", b.toString()));
            while (result.hasNext()) {
                Map<String, Object> resultElement = result.next();
                List<String> path = (List<String>) resultElement.get("path");
                try {
                    paths.add(Lists.transform(path, UUID::fromString));
                } catch (IllegalArgumentException ia) {
                    throw new IllegalArgumentException("Your metadata is corrupt", ia);
                }
            }
            tx.success();
        }
        return paths;
    }

    private List<UUID> readMetasFrom(Node node, String propertyKey) {
        return Lists.transform(Arrays.asList((String[]) node.getProperty(propertyKey)), UUID::fromString);
    }

    public void close() {
        graphDb.shutdown();
    }

    /**
     * Create the given block or ignore the block. Blocks are immutable, so ignore them if it already exists
     *
     * @param block
     */
    public void createOrIgnore(MetadataBlock block) {
        try (Transaction tx = graphDb.beginTx()) {
            Node metaNode = graphDb.findNode(metadataLabel, "ident", block.ident.toString());
            if (metaNode == null) {
                Node newNode = graphDb.createNode(metadataLabel);
                copyProperties(block, newNode);
                for (UUID childUUID : block.metas) {
                    Node childNode = graphDb.findNode(metadataLabel, "ident", childUUID.toString());
                    newNode.createRelationshipTo(childNode, UnitransRelationshipTypes.CHILD_ELEMENT);
                }
            }
            tx.success();
        }
    }

    /**
     * paste block properties onto newnode
     *
     * @param block
     * @param node
     */
    public static void copyProperties(MetadataBlock block, Node node) {
        node.setProperty("ident", block.ident.toString());
        node.setProperty("metas", Lists.transform(block.metas, UUID::toString).toArray(new String[]{}));
        node.setProperty("datas", block.datas.toArray(new String[]{}));
    }
}
