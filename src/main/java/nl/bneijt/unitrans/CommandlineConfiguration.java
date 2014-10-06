package nl.bneijt.unitrans;

import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;

public class CommandlineConfiguration {

    public static final String BLOCKSTORE_LOCATION = "blockstore";
    public static final String BLOCKSTORE_LOCATION_DEFAULT = new File(new File(System.getProperty("user.home")), "tmp/unitrans/blockstore").toString();
    public static final String ROOT_BLOCK_PER_USER_LOCATION_DEFUALT = new File(new File(System.getProperty("user.home")), "tmp/unitrans/rootBlockPerUser.properties").toString();

    public static final String WEB_RESOURCES_LOCATION = "webresources";
    public static final String WEB_RESOURCES_LOCATION_DEFAULT = "src/main/resources/webapp";

    public static final String SERVER_PORT = "serverport";
    public static final int SERVER_PORT_DEFAULT = 8443;

    private final Namespace namespace;

    public CommandlineConfiguration(Namespace namespace) {
        this.namespace = namespace;
    }

    public String getBlockstoreLocation() {
        return namespace.getString(BLOCKSTORE_LOCATION);
    }

    public int getServerPort() {
        return namespace.getInt(SERVER_PORT);
    }

    public String getRootBlockPerUserLocation() {
        return ROOT_BLOCK_PER_USER_LOCATION_DEFUALT;

    }

    public String getWebResourcesLocation() {
        return namespace.getString(WEB_RESOURCES_LOCATION);
    }
}
