package nl.bneijt.unitrans.accesscontrol;

import nl.bneijt.unitrans.blockstore.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

public class AccessControl {

    private final Users users;
    private final DiskMap rootMetaBlockFor;
    private Logger logger = LoggerFactory.getLogger(AccessControl.class);

    @Inject
    public AccessControl(@Named("rootBlockPerUser") DiskMap rootMetaBlockFor, Users users) {
        this.rootMetaBlockFor = rootMetaBlockFor;
        this.users = users;
    }

    public boolean canWrite(User user) {
        return true;
    }

    public boolean canAccess(User user) {
        return users.isKnown(user);
    }

    public Hash getRootMetaBlockFor(User user) {
        String metaBlockHash = rootMetaBlockFor.getOrNull(user.publicDigestUrl());
        logger.debug("Current root block for {} is {}", user, metaBlockHash);
        if (metaBlockHash == null) {
            return null;
        }
        return Hash.fromBase64(metaBlockHash);
    }

    public void setRootMetaBlockFor(User user, Hash rootIdentifier) throws IOException {
        rootMetaBlockFor.put(user.publicDigestUrl(), rootIdentifier.toBase64());
    }
}
