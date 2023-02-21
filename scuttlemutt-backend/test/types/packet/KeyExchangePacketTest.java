package types.packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.PublicKey;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import crypto.Crypto;
import types.TestUtils;

public class KeyExchangePacketTest {
    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a KeyExchangePacket object.
        PublicKey pubKey = Crypto.ALICE_KEYPAIR.getPublic();
        SecretKey secKey = Crypto.DUMMY_SECRETKEY;
        new KeyExchangePacket(pubKey, secKey, TestUtils.generateRandomizedDawgIdentifier());
    }

    @Test
    public void testGetPacketContents_returnsContents() {
        PublicKey pubKey = Crypto.ALICE_KEYPAIR.getPublic();
        SecretKey secKey = Crypto.DUMMY_SECRETKEY;
        
        // Successfully create a KeyExchangePacket object.
        final KeyExchangePacket packet = new KeyExchangePacket(pubKey, secKey, TestUtils.generateRandomizedDawgIdentifier());

        // Obtain the packet contents.
        final PublicKey packetPublicKey = packet.getPublicKey();
        final SecretKey packetSecretKey = packet.getSecretKey();

        // Assert that the contents are as expected.
        assertEquals(Crypto.ALICE_KEYPAIR.getPublic(), packetPublicKey);
        assertEquals(Crypto.DUMMY_SECRETKEY, packetSecretKey);
    }

    @Test
    public void testEquals_differentObjectsButSameKeys_returnsTrue() {
        PublicKey pubKey = Crypto.ALICE_KEYPAIR.getPublic();
        SecretKey secKey = Crypto.DUMMY_SECRETKEY;

        // Create two KeyExchangePacket objects with the same PublicKey.
        final KeyExchangePacket packet1 = new KeyExchangePacket(pubKey, secKey, TestUtils.generateRandomizedDawgIdentifier());
        final KeyExchangePacket packet2 = new KeyExchangePacket(pubKey, secKey, TestUtils.generateRandomizedDawgIdentifier());

        // Verify that the two KeyExchangePacket objects are equal.
        assertEquals(packet1, packet2);
    }

    @Test
    public void testEquals_differentObjectsAndDifferentKeys_returnsFalse() {
        PublicKey pubKey1 = Crypto.ALICE_KEYPAIR.getPublic();
        PublicKey pubKey2 = Crypto.BOB_KEYPAIR.getPublic();
        SecretKey secKey1 = Crypto.DUMMY_SECRETKEY;
        SecretKey secKey2 = Crypto.OTHER_SECRETKEY;

        // Create four KeyExchangePacket objects with the different keys.
        final KeyExchangePacket packet1 = new KeyExchangePacket(pubKey1, secKey1, TestUtils.generateRandomizedDawgIdentifier());
        final KeyExchangePacket packet2 = new KeyExchangePacket(pubKey1, secKey2, TestUtils.generateRandomizedDawgIdentifier());
        final KeyExchangePacket packet3 = new KeyExchangePacket(pubKey2, secKey1, TestUtils.generateRandomizedDawgIdentifier());
        final KeyExchangePacket packet4 = new KeyExchangePacket(pubKey2, secKey2, TestUtils.generateRandomizedDawgIdentifier());

        // Verify that all KeyExchangePacket objects are different.
        assertNotEquals(packet1, packet2);
        assertNotEquals(packet1, packet3);
        assertNotEquals(packet1, packet4);
        assertNotEquals(packet2, packet3);
        assertNotEquals(packet2, packet4);
        assertNotEquals(packet3, packet4);
    }
}
