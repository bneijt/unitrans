package nl.bneijt.unitrans.blockstore;

import com.google.common.base.Splitter;

import java.io.File;

public class HashDirectoryTree {
    public final File basePath;
    final Splitter blockNameSplitter = Splitter.fixedLength(4).limit(3);

    public HashDirectoryTree(File basePath) {
        this.basePath = basePath;
    }



    public File locationFor(String hash) {
        Iterable<String> identifierElements = blockNameSplitter.split(hash);
        File location = basePath;
        for (String identifierElement : identifierElements) {
            location = new File(location, identifierElement);
        }
        return location;
    }
}
