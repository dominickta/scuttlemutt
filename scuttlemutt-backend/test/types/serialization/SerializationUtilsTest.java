package types.serialization;

import crypto.Crypto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import javax.crypto.SecretKey;

public class SerializationUtilsTest {
    @Test
    public void testSecretKeySerializationLifecycle() {
        // create a SecretKey.
        final SecretKey key = Crypto.DUMMY_SECRETKEY;

        // serialize the SecretKey.
        final byte[] secretKeyBytes = SerializationUtils.serializeKey(key);

        // deserialize the SecretKey's byte[].
        final SecretKey deserializedKey = SerializationUtils.deserializeSecretKey(secretKeyBytes);

        // assert that the deserialized key is identical to the original PublicKey.
        assertArrayEquals(key.getEncoded(), deserializedKey.getEncoded());
    }
}
