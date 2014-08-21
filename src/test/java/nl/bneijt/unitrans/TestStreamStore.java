package nl.bneijt.unitrans;

import nl.bneijt.unitrans.blockstore.MetaData;
import nl.bneijt.unitrans.blockstore.StreamStore;

import java.io.IOException;
import java.io.InputStream;

public class TestStreamStore implements StreamStore {
    @Override
    public MetaData writeStream(InputStream inputStream) throws IOException {
        return null;
    }

    @Override
    public InputStream readStream(MetaData root) throws IOException {
        return null;
    }
}
