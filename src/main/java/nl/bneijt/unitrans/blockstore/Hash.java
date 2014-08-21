package nl.bneijt.unitrans.blockstore;

import com.google.common.io.BaseEncoding;

public class Hash {
    private final byte[] bytes;

    public Hash(byte[] digestValue) {
        bytes = digestValue;
    }

    /** Used internally as a more compact string
     *
     * @return The base64 encoded string of this digest
     */
    public String toBase64() {
        return BaseEncoding.base64().encode(bytes);
    }
    public static Hash fromBase64(String hash) {
        return new Hash(BaseEncoding.base64().decode(hash));
    }

    /** The external representation of this Hash
     *
     * @return A base16 encoded hex string of the bytes
     */
    public String toBase16() {
        return BaseEncoding.base16().encode(bytes);
    }
    public static Hash fromBase16(String hash) {
        return new Hash(BaseEncoding.base16().decode(hash));
    }
}
