package types.serialization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class SerializationUtilsTest {
    @Test
    public void testSecretKeySerializationLifecycle() {
        // create a SecretKey.
        final SecretKey key = Crypto.DUMMY_SECRETKEY;

        // serialize the SecretKey.
        final byte[] secretKeyBytes = SerializationUtils.serializeKey(key);

        // deserialize the SecretKey's byte[].
        final SecretKey deserializedKey = SerializationUtils.deserializeSecretKey(secretKeyBytes);

        // assert that the deserialized key is identical to the original SecretKey.
        assertArrayEquals(key.getEncoded(), deserializedKey.getEncoded());
    }

    @Test
    public void testPublicKeySerializationLifecycle() {
        // create a PublicKey.
        final PublicKey key = Crypto.ALICE_KEYPAIR.getPublic();

        // serialize the PublicKey.
        final byte[] pubKeyBytes = SerializationUtils.serializeKey(key);

        // deserialize the PublicKey's byte[].
        final PublicKey deserializedKey = SerializationUtils.deserializePublicKey(pubKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertArrayEquals(key.getEncoded(), deserializedKey.getEncoded());
    }
}
