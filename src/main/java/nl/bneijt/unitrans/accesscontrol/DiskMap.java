package nl.bneijt.unitrans.accesscontrol;

import java.io.IOException;

public interface DiskMap {
    String getOrNull(String key);

    void put(String key, String value) throws IOException;
}
