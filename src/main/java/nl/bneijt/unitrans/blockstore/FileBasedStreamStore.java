package nl.bneijt.unitrans.blockstore;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public class FileBasedStreamStore implements StreamStore {

    final BlockStore blockStore;

    @Inject
    public FileBasedStreamStore(FileBasedBlockStore blockStore) {
        this.blockStore = blockStore;
    }

    @Override
    public MetaData writeStream(final InputStream inputStream) throws IOException {
        MetaData metaData = new MetaData();
        //Write parts of the file to separate blocks
        final Range<Long> blockRange = blockStore.blockSize();
        while (true) {
            final int nextByte = inputStream.read();
            if (nextByte < 0) {
                //EOF
                break;
            }
            HeadInputStream headInputStream = new HeadInputStream(inputStream, blockRange.upperEndpoint());
            Hash newBlock = blockStore.writeBlock(headInputStream);
            metaData.addBlock(newBlock);
        }
        return metaData;
    }

    @Override
    public InputStream readStream(MetaData root) throws IOException {
        //Generate a list of all blocks we need
        //Feed each block from the inputStream

        return new BlockChainInputStream(blockStore, root.getMetaBlockHashList(), root.getBlockHashList());
    }
}
