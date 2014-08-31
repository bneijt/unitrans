package nl.bneijt.unitrans;

import nl.bneijt.unitrans.accesscontrol.DiskMap;

import java.io.IOException;

public class TestRootBlockPerUserDiskMap implements DiskMap {

    @Override
    public String getOrNull(String key) {
        throw new RuntimeException("This method needs to be implemented by Bram");
    }

    @Override
    public void put(String key, String value) throws IOException {
        throw new RuntimeException("This method needs to be implemented by Bram");
    }
}
