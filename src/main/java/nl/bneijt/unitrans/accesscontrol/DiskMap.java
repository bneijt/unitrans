package nl.bneijt.unitrans.accesscontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class DiskMap {

    private final Properties properties;
    private final File propertiesLocation;
    private Logger logger = LoggerFactory.getLogger(DiskMap.class);

    public DiskMap(File location) throws IOException {
        properties = new Properties();
        if(location.exists()) {
            FileInputStream input = new FileInputStream(location);
            properties.load(input);
        }
        this.propertiesLocation = location;
    }

    public String getOrNull(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.debug("Could not find {} in diskmap: {}", key);
        }
        return value;
    }

    public void put(String key, String value) throws IOException {
        properties.setProperty(key, value);
        synchronized (propertiesLocation) {
            FileOutputStream outputStream = new FileOutputStream(propertiesLocation);
            properties.store(outputStream, null);
        }
    }
}
