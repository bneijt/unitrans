package nl.bneijt.unitrans;

import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;

public class CommandlineConfiguration {

    public static final String DATA_LOCATION = "data";
    public static final String DATA_LOCATION_DEFAULT = new File(new File(System.getProperty("user.home")), "tmp/unitrans/data").toString();

    public static final String META_LOCATION = "meta";
    public static final String META_LOCATION_DEFAULT = new File(new File(System.getProperty("user.home")), "tmp/unitrans/meta").toString();

    public static final String WEB_RESOURCES_LOCATION = "webresources";
    public static final String WEB_RESOURCES_LOCATION_DEFAULT = "src/main/resources/webapp";

    public static final String SERVER_PORT = "serverport";
    public static final int SERVER_PORT_DEFAULT = 8443;

    private final Namespace namespace;

    public CommandlineConfiguration(Namespace namespace) {
        this.namespace = namespace;
    }

    public File getDataStoreLocation() {
        return new File(namespace.getString(DATA_LOCATION));
    }

    public File getMetaStoreLocation() {
        return new File(namespace.getString(META_LOCATION));
    }

    public int getServerPort() {
        return namespace.getInt(SERVER_PORT);
    }

    public String getWebResourcesLocation() {
        return namespace.getString(WEB_RESOURCES_LOCATION);
    }

    public String getKeyStorePassword() {
        return "53c3059b53b5808cc25cbbcb6d3ba90d8f78c0cb";
    }

    public File getKeyStoreLocation() {
        return new File(getMetaStoreLocation(), "keystore.jks");
    }
}
