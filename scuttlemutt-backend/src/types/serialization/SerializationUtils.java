package types.serialization;

import java.security.Key;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import crypto.Crypto;

public class SerializationUtils {

    public static byte[] serializeKey(final Key k) {
        return Base64.getEncoder().encode(k.getEncoded());
    }

    public static SecretKey deserializeSecretKey(final byte[] keyBytes) {
        final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, Crypto.SYMMETRIC_KEY_TYPE);
    }

    public static PublicKey deserializePublicKey(final byte[] keyBytes) {
        final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);
        return (PublicKey) new X509EncodedKeySpec(encodedKey);
    }
}
