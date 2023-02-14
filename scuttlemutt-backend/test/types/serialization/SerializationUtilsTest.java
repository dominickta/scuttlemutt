package types.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.PublicKey;

import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class SerializationUtilsTest {
    @Test
    public void testPublicKeySerializationLifecycle() {
        // create a PublicKey.
        final PublicKey publicKey = Crypto.alice.getPublic();

        // serialize the PublicKey.
        final byte[] publicKeyBytes = SerializationUtils.serializeKey(publicKey);

        // deserialize the PublicKey's byte[].
        final PublicKey deserializedPublicKey = SerializationUtils.deserializeRSAPublicKey(publicKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertEquals(publicKey, deserializedPublicKey);
    }
}
