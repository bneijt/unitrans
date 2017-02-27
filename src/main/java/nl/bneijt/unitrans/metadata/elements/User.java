package nl.bneijt.unitrans.metadata.elements;

import java.util.List;
import java.util.UUID;

public class User {
    public final String username;
    public final List<UUID> rootMetadataBlocks;

    public User(String username, List<UUID> rootMetadataBlocks) {
        this.username = username;
        this.rootMetadataBlocks = rootMetadataBlocks;
    }
}
