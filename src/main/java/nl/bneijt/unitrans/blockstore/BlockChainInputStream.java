package nl.bneijt.unitrans.blockstore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Input stream that generated the given bytes for each block
 */
public class BlockChainInputStream extends InputStream {

    private final List<Hash> nextBlockList;
    private final List<Hash> nextMetaBlocks;

    private final BlockStore blockStore;
    private InputStream currentStream;

    public BlockChainInputStream(BlockStore blockStore, List<Hash> metaDataBlocks, List<Hash> blocks) throws IOException {
        this.blockStore = blockStore;

        this.nextMetaBlocks = new ArrayList<Hash>();
        this.nextMetaBlocks.addAll(metaDataBlocks);

        this.nextBlockList = new ArrayList<Hash>();
        this.nextBlockList.addAll(blocks);

        //Set first stream
        switchToNextStream();
    }

    private void switchToNextStream() throws IOException {
        if (nextBlockList.size() > 0) {
            currentStream = blockStore.openBlock(nextBlockList.get(0));
            nextBlockList.remove(0);
            return;
        }
        if (nextMetaBlocks.size() > 0) {
            MetaData nextMetaBlock = blockStore.readMeta(nextMetaBlocks.get(0));
            nextMetaBlocks.remove(0);
            nextBlockList.addAll(nextMetaBlock.getBlockHashList());
            nextMetaBlocks.addAll(nextMetaBlock.getMetaBlockHashList());
            return;
        }

        currentStream = new ByteArrayInputStream(new byte[]{});
        return;
    }


    @Override
    public int read() throws IOException {
        int read = currentStream.read();
        if (read < 0) {
            switchToNextStream();
            return currentStream.read();
        }
        return read;
    }
}
