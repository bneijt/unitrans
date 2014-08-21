package nl.bneijt.unitrans.blockstore;

import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class DigestTest {

    @Test
    public void sizeShouldBeResultSizeAfterEncoding() throws NoSuchAlgorithmException {
        Digest digest = new Digest();
        String hash = digest.update(new byte[]{0x50}).encode();
        assertThat(Digest.HEX_SIZE, is(hash.length()));
    }

    @Test
    public void differentBytesShouldLeadToDifferentDigests() {
        String hashA = Digest.hashOf(new byte[]{0x52});
        String hashB = Digest.hashOf(new byte[]{0x51});
        assertThat(hashA, not(equalTo((hashB))));

    }
}
