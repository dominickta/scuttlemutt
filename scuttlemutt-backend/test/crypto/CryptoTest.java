package crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CryptoTest {

    // runtime-defined test objects
    private KeyPair alice;
    private KeyPair bob;

    @BeforeEach
    public void setup() {
        this.alice = Crypto.alice;
        this.bob = Crypto.bob;
    }

    @Test
    public void testEncryptThenDecryptGivesSameMessage() {
        // create a dummy message
        String message = "Foo";
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

        // encrypt the message using alice's public key
        byte[] encrypted = Crypto.encrypt(bytes, alice.getPublic());

        // decrypt the message using alice's private key
        byte[] decrypted = Crypto.decrypt(encrypted, alice.getPrivate());

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
