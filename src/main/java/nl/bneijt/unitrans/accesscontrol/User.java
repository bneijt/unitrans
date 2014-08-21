package nl.bneijt.unitrans.accesscontrol;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;

public class User implements Comparable<User> {
    private final byte[] publicDigest;

    public User(byte[] publicDigest) {
        Preconditions.checkNotNull(publicDigest);
        this.publicDigest = publicDigest;
    }

    public String publicDigestUrl() {
        return BaseEncoding.base64Url().encode(publicDigest);
    }

    @Override
    public String toString() {
        return "user/" + publicDigestUrl();
    }

    @Override
    public int compareTo(User user) {
        if (user.publicDigest.length != publicDigest.length) {
            return Integer.compare(publicDigest.length, user.publicDigest.length);
        }
        for (int i = 0; i < publicDigest.length; i++) {
            int d = Byte.compare(publicDigest[i], user.publicDigest[i]);
            if (d != 0) {
                return d;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!Arrays.equals(publicDigest, user.publicDigest)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(publicDigest);
    }
}
