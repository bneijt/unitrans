package nl.bneijt.unitrans.resources.elements;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import nl.bneijt.unitrans.blockstore.Hash;
import nl.bneijt.unitrans.blockstore.MetaData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MetaDataElement {

    public final String id;
    public final List<String> blockList;
    public final List<String> metaBlockList;
    public final Map<String, String> meta;

    public MetaDataElement(String id, List<String> blockList, List<String> metaBlockList, Map<String, String> meta) {
        this.id = id;
        this.blockList = blockList;
        this.metaBlockList = metaBlockList;
        this.meta = meta;
    }


    public static MetaDataElement from(MetaData metaData, Hash id) {
        MetaDataElement metaDataElement = new MetaDataElement(
                id.toBase16(),
                Lists.transform(metaData.getBlockHashList(), new Function<Hash, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Hash hash) {
                        return hash.toBase16();
                    }
                }),
                Lists.transform(metaData.getMetaBlockHashList(), new Function<Hash, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable Hash hash) {
                        return hash.toBase16();
                    }
                }),
                metaData.getMetaData()
        );
        return metaDataElement;
    }
}
