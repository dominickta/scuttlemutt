package types.packet;

import crypto.Crypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class KeyExchangePacketTest {
    private PublicKey publicKey1, publicKey2;

    @BeforeEach
    public void setup() {
        this.publicKey1 = Crypto.generateKeyPair().getPublic();
        this.publicKey2 = Crypto.generateKeyPair().getPublic();
    }

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a KeyExchangePacket object.
        final KeyExchangePacket kePacket = new KeyExchangePacket(publicKey1);
    }

    @Test
    public void testGetPacketContents_returnsContents(){
        // Successfully create a KeyExchangePacket object.
        final KeyExchangePacket kePacket = new KeyExchangePacket(publicKey1);

        // Obtain the packet contents.
        final PublicKey kePacketContents = kePacket.getPublicKey();

        // Assert that the contents are as expected.
        assertEquals(publicKey1, kePacketContents);
    }

    @Test
    public void testEquals_differentObjectsButSameBarks_returnsTrue() {
        // Create two KeyExchangePacket objects with the same PublicKey.
        final KeyExchangePacket kePacket1 = new KeyExchangePacket(publicKey1);
        final KeyExchangePacket kePacket2 = new KeyExchangePacket(publicKey1);

        // Verify that the two KeyExchangePacket objects are equal.
        assertEquals(kePacket1, kePacket2);
    }

    @Test
    public void testEquals_differentObjectsAndDifferentBytes_returnsFalse() {
        // Create two KeyExchangePacket objects with the different PublicKeys.
        final KeyExchangePacket kePacket1 = new KeyExchangePacket(publicKey1);
        final KeyExchangePacket kePacket2 = new KeyExchangePacket(publicKey2);

        // Verify that the two KeyExchangePacket objects are different.
        assertNotEquals(kePacket1, kePacket2);
    }
}
