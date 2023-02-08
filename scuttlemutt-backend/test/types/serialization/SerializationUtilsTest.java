package types.serialization;

import crypto.Crypto;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationUtilsTest {
    @Test
    public void testPublicKeySerializationLifecycle() {
        // create a PublicKey.
        final PublicKey publicKey = Crypto.generateKeyPair().getPublic();

        // serialize the PublicKey.
        final byte[] publicKeyBytes = SerializationUtils.serializeKey(publicKey);

        // deserialize the PublicKey's byte[].
        final PublicKey deserializedPublicKey = SerializationUtils.deserializeRSAPublicKey(publicKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertEquals(publicKey, deserializedPublicKey);
    }
}
