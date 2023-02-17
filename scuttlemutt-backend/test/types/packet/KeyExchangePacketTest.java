package types.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.Key;

import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class KeyExchangePacketTest {
    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a KeyExchangePacket object.
        final KeyExchangePacket kePacket = new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);
    }

    @Test
    public void testGetPacketContents_returnsContents() {
        // Successfully create a KeyExchangePacket object.
        final KeyExchangePacket kePacket = new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);

        // Obtain the packet contents.
        final Key kePacketContents = kePacket.getKey();

        // Assert that the contents are as expected.
        assertEquals(Crypto.DUMMY_SECRETKEY, kePacketContents);
    }

    @Test
    public void testEquals_differentObjectsButSameKeys_returnsTrue() {
        // Create two KeyExchangePacket objects with the same PublicKey.
        final KeyExchangePacket kePacket1 = new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);
        final KeyExchangePacket kePacket2 = new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);

        // Verify that the two KeyExchangePacket objects are equal.
        assertEquals(kePacket1, kePacket2);
    }

    @Test
    public void testEquals_differentObjectsAndDifferentKeys_returnsFalse() {
        // Create two KeyExchangePacket objects with the different PublicKeys.
        final KeyExchangePacket kePacket1 = new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);
        final KeyExchangePacket kePacket2 = new KeyExchangePacket(Crypto.generateSecretKey());

        // Verify that the two KeyExchangePacket objects are different.
        assertNotEquals(kePacket1, kePacket2);
    }
}
