package nl.bneijt.unitrans.blockstore;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class representing MetaData
 * Used to store references to other blocks and metadata information of blocks below them.
 */
public class MetaData {
    public static final String CREATED = "created";
    public static final String NAME = "name";


    private final Map<String, String> metaDataMap;
    private final List<Hash> blockHashList;
    private final List<Hash> metaBlockHashList;

    public MetaData(Map<String, String> metaDataMap, List<Hash> blockHashList, List<Hash> metaBlockHashList) {
        Preconditions.checkNotNull(metaDataMap);
        Preconditions.checkNotNull(blockHashList);
        Preconditions.checkNotNull(metaBlockHashList);
        this.metaDataMap = new HashMap<>();
        this.metaDataMap.putAll(metaDataMap);

        this.blockHashList = new ArrayList<>();
        this.blockHashList.addAll(blockHashList);

        this.metaBlockHashList = new ArrayList<>();
        this.metaBlockHashList.addAll(metaBlockHashList);
    }

    public MetaData() {
        metaDataMap = new HashMap<>();
        blockHashList = new ArrayList<>();
        metaBlockHashList = new ArrayList<>();
    }

    public void addBlock(Hash hash) {
        Preconditions.checkNotNull(hash, "You can not add null blocks");
        blockHashList.add(hash);
    }
    public List<Hash> getBlockHashList() {
        return ImmutableList.copyOf(blockHashList);
    }

    public void addMetaBlock(Hash hash) {
        Preconditions.checkNotNull(hash, "You can not add null blocks");
        metaBlockHashList.add(hash);
    }
    public List<Hash> getMetaBlockHashList() {
        return ImmutableList.copyOf(metaBlockHashList);
    }


    public void putMetaData(String key, String value) {
        metaDataMap.put(key, value);
    }

    public Map<String, String> getMetaData() {
        return metaDataMap;
    }
}
