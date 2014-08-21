package nl.bneijt.unitrans.blockstore;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.Random;

public class HashTest {
    @Test
    public void testBase16() throws Exception {
        Hash hash = new Hash(new byte[]{0x20, 0x10, 0x33});
        MatcherAssert.assertThat(hash.toBase16(), Is.is("201033"));
    }

    public static Hash randomHash() {
        byte[] bytes = new byte[Digest.HEX_SIZE / 8];
        Random random = new Random();
        random.nextBytes(bytes);
        return new Hash(bytes);
    }
}
