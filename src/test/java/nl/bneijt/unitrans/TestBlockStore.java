package nl.bneijt.unitrans;

import com.google.common.collect.Range;
import nl.bneijt.unitrans.blockstore.BlockStore;
import nl.bneijt.unitrans.blockstore.Hash;
import nl.bneijt.unitrans.blockstore.HashTest;
import nl.bneijt.unitrans.blockstore.MetaData;

import java.io.IOException;
import java.io.InputStream;

public class TestBlockStore implements BlockStore {
    @Override
    public InputStream openBlock(Hash identifier) throws IOException {
        return null;
    }

    @Override
    public Hash writeBlock(InputStream inputStream) throws IOException {
        return HashTest.randomHash();
    }

    @Override
    public Range<Long> blockSize() {
        return Range.closed(1l, 3l);
    }

    @Override
    public MetaData readMeta(Hash identifier) throws IOException {
        return null;
    }

    @Override
    public Hash writeMeta(MetaData metaData) throws IOException {
        return HashTest.randomHash();
    }
}
