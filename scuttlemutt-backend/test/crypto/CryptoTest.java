package crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

public class CryptoTest {

    // runtime-defined test objects
    private static final KeyPair alice = Crypto.ALICE_KEYPAIR;
    private static final KeyPair bob = Crypto.BOB_KEYPAIR;
    private static final SecretKey secretKey = Crypto.DUMMY_SECRETKEY;

    @Test
    public void testEncryptThenDecryptGivesSameMessage_asymmetric() {
        // create a dummy message
        String message = "Foo";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        // encrypt the message using alice's public key
        byte[] encrypted = Crypto.encrypt(bytes, alice.getPublic(), Crypto.ASYMMETRIC_KEY_TYPE);

        // decrypt the message using alice's private key
        byte[] decrypted = Crypto.decrypt(encrypted, alice.getPrivate(), Crypto.ASYMMETRIC_KEY_TYPE);

        // verify the strings match
        String new_message = new String(decrypted, StandardCharsets.UTF_8);
        assertEquals(message, new_message);
    }

    @Test
    public void testEncryptThenDecryptGivesSameMessage_symmetric() {
        // create a dummy message
        String message = "Foo";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        // encrypt the message using the secret key
        byte[] encrypted = Crypto.encrypt(bytes, secretKey, Crypto.SYMMETRIC_KEY_TYPE);

        // decrypt the message using the secret key
        byte[] decrypted = Crypto.decrypt(encrypted, secretKey, Crypto.SYMMETRIC_KEY_TYPE);

        // verify the strings match
        String new_message = new String(decrypted, StandardCharsets.UTF_8);
        assertEquals(message, new_message);
    }

    @Test
    public void testVerifyValidSignaturePasses() {
        // create a dummy message
        String message = "Foo";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        // sign the message using alice's private key
        byte[] signature = Crypto.sign(bytes, alice.getPrivate());

        // verify the signature came from alice using alice's public key
        boolean isValid = Crypto.verify(signature, bytes, alice.getPublic());
        assertTrue(isValid);
    }

    @Test
    public void testVerifyInvalidSignatureFails() {
        // create a dummy message
        String message = "Foo";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        // sign the message using alice's private key
        byte[] signature = Crypto.sign(bytes, alice.getPrivate());

        // try to verify the signature came from bob using bob's public key
        boolean isValid = Crypto.verify(signature, bytes, bob.getPublic());
        assertFalse(isValid);
    }
}
