package nl.bneijt.unitrans.accesscontrol;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Singleton
public class RootBlockPerUserDiskMap extends FileDiskMap {
    public RootBlockPerUserDiskMap(File location) throws IOException {
        super(location);
    }
}
