import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtil {
    private static final MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }

    public static synchronized int hash(String input, int m) {
        md.reset();
        byte[] digest = md.digest(input.getBytes());
        int hash = 0;
        for (int i = 0; i < Math.min(4, digest.length); i++) {
            hash = (hash << 8) + (digest[i] & 0xFF);
        }
        return Math.abs(hash) % (int) Math.pow(2, m);
    }
}
