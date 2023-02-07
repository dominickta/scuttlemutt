package types.serialization;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class SerializationUtils {

    public static byte[] serializeKey(final PublicKey k) {
        return k.getEncoded();
    }

    public static PublicKey deserializeRSAPublicKey(final byte[] keyBytes) {
        try {
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException e) {
            // occurs if there are issues when creating the KeyFactory.
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e) {
            // occurs if there are issues with the regeneration.
            throw new RuntimeException(e);
        }
    }
}
