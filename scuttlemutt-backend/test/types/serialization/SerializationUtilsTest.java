package types.serialization;

import crypto.Crypto;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SerializationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

public class SerializationUtilsTest {
    private static final int NUM_KEYS_IN_LIST = 10;  // the number of Keys in the List when
                                                     // testing List<Key>-related methods.

    @Test
    public void testSecretKeySerializationLifecycle() {
        // get a SecretKey.
        final SecretKey key = Crypto.DUMMY_SECRETKEY;

        // serialize the SecretKey.
        final byte[] secretKeyBytes = SerializationUtils.serializeKey(key);

        // deserialize the SecretKey's byte[].
        final SecretKey deserializedKey = (SecretKey) SerializationUtils.deserializeKey(secretKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertArrayEquals(key.getEncoded(), deserializedKey.getEncoded());
    }

    @Test
    public void testPublicKeySerializationLifecycle() {
        // create a PublicKey.
        final PublicKey publicKey = Crypto.ALICE_KEYPAIR.getPublic();

        // serialize the PublicKey.
        final byte[] publicKeyBytes = SerializationUtils.serializeKey(publicKey);

        // deserialize the PublicKey's byte[].
        final PublicKey deserializedPublicKey = (PublicKey) SerializationUtils.deserializeKey(publicKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test
    public void testKeyListSerializationLifecycle() {
        // generate the Keys we're using for testing.
        final List<Key> keyList = new ArrayList<Key>();
        for (int i = 0; i < NUM_KEYS_IN_LIST; i++) {
            keyList.add(Crypto.generateSecretKey());
        }

        // serialize the keyList.
        final String keyListSerialization = SerializationUtils.serializeKeyList(keyList);

        // deserialize the keyListSerialization.
        final List<Key> deserializedKeyList = SerializationUtils.deserializeKeyList(keyListSerialization);

        // assert that the deserialized List<Key> is identical to the original List<Key>.
        assertEquals(keyList, deserializedKeyList);
    }

    @Test
    public void testDeserializeKey_whenProvidedRandomBytes_throwsSerializationException() {
        // create some random byte[] to feed the method.
        final byte[] randomBytes = RandomStringUtils.randomAlphanumeric(15).getBytes();

        // assert that the exception is thrown when attempting to deserialize the byte[].
        assertThrows(SerializationException.class, () -> SerializationUtils.deserializeKey(randomBytes));
    }
}
