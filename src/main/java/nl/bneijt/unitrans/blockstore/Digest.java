package nl.bneijt.unitrans.blockstore;

import com.google.common.io.BaseEncoding;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * For example: 9fbbbb5a0f329f9782e2356fa41d89cf9b3694327c1a934d6af2a9df2d7f936ce83717fb513196a4ce5548471708cd7134c2ae99b3c357bcabb2eafc7b9b7570
 */
public class Digest {
    public static final int HEX_SIZE = 128;

    private final MessageDigest digest;

    public Digest() {
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Your platform does not support the SHA-512 digest.");
        }
    }

    public Digest update(byte[] bytes) {
        digest.update(bytes);
        return this;
    }

    public String encode() {
        byte[] hash = digest.digest();
        return BaseEncoding.base16().encode(hash).toLowerCase();

    }

    public static String hashOf(byte[] bytes) {
        Digest digest = new Digest();
        digest.update(bytes);
        return digest.encode();
    }

    public static MessageDigest defaultMessageDigest() {
        return new Digest().digest;
    }
}
