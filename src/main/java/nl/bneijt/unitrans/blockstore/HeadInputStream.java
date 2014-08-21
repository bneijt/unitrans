package nl.bneijt.unitrans.blockstore;

import com.google.common.collect.Range;

import java.io.IOException;
import java.io.InputStream;

public class HeadInputStream extends InputStream{
    private final InputStream in;
    private long length;

    public HeadInputStream(InputStream in, long size) {

        this.in = in;
        this.length = size;
    }

    @Override
    public int read() throws IOException {
        if(--length > 0) {
            return in.read();
        }
        return -1;
    }
}
