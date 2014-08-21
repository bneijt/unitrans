package nl.bneijt.unitrans.blockstore;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.ListTemplate;
import org.msgpack.template.MapTemplate;
import org.msgpack.template.StringTemplate;
import org.msgpack.unpacker.Unpacker;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.Map;

public class MetaDataSerializer {


    public static void writeTo(MetaData metaData, OutputStream outputStream) throws IOException {
        MessagePack msgpack = new MessagePack();
        Packer packer = msgpack.createPacker(outputStream);

        Map<String, String> map = metaData.getMetaData();
        packer.write(map);
        packer.write(toBase64(metaData.getBlockHashList()));
        packer.write(toBase64(metaData.getMetaBlockHashList()));
        packer.close();
    }


    public static MetaData readFrom(InputStream file) throws IOException {
        MessagePack msgpack = new MessagePack();
        Unpacker unpacker = msgpack.createUnpacker(file);

        Map<String, String> metaData = unpacker.read(new MapTemplate<>(StringTemplate.getInstance(), StringTemplate.getInstance()));

        List<Hash> blockHashList = fromBase64(unpacker.read(new ListTemplate<>(StringTemplate.getInstance())));
        List<Hash> metaBlockHashList = fromBase64(unpacker.read(new ListTemplate<>(StringTemplate.getInstance())));

        return new MetaData(metaData, blockHashList, metaBlockHashList);

    }

    private static List<Hash> fromBase64(List<String> blocks) {
        return Lists.transform(blocks, new Function<String, Hash>() {
            @Nullable
            @Override
            public Hash apply(@Nullable String s) {
                return Hash.fromBase64(s);
            }
        });
    }

    private static List<String> toBase64(List<Hash> blocks) {
        return Lists.transform(blocks, new Function<Hash, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Hash hash) {
                if (hash == null) {
                    throw new IllegalStateException("A block in the metadata block list way null, somebody put dirty data in the constructor?");
                }
                return hash.toBase64();
            }
        });
    }


}
