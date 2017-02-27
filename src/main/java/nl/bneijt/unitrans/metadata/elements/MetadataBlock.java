package nl.bneijt.unitrans.metadata.elements;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MetadataBlock {
    public final UUID ident;
    public final List<UUID> metas;
    public final List<String> datas;

    public MetadataBlock(UUID ident, List<UUID> metas, List<String> datas) {
        this.ident = ident;
        this.metas = metas;
        this.datas = datas;
    }


    public MetadataBlock(MetadataBlock other) {
        this.ident = UUID.randomUUID();
        this.metas = Lists.newArrayList(other.metas);
        this.datas = Lists.newArrayList(other.datas);
    }

    public static MetadataBlock emptyRandomBlock() {
        return new MetadataBlock(UUID.randomUUID(), Collections.emptyList(), Collections.emptyList());
    }
}
