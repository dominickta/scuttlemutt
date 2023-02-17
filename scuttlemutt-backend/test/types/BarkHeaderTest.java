package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class BarkHeaderTest {
    @Test
    public void testConstructor_createsObjectSuccessfully() {
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();

        final BarkHeader b = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE);
        assertNotNull(b);
    }

    @Test
    public void testEquals_getSender() {
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();

        final BarkHeader b = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE);
        assertEquals(b.getSender(), alice);
    }

    @Test
    public void testEquals_getReceiver() {
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();

        final BarkHeader b = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE);
        assertEquals(b.getReceiver(), bob);
    }

    @Test
    public void testEquals_getOrderNum() {
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();

        final BarkHeader b = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE);
        assertEquals(b.getOrderNum(), 0L);
    }

    @Test
    public void testEquals_getFillerCount() {
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();

        final BarkHeader b = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE - 42);
        assertEquals(b.getFillerCount(), 42);
    }

    @Test
    public void testByteConversion_toEncryptedBytes_fromEncryptedBytes_identicalObject() {
        // create bark header
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();
        final BarkHeader barkHeader = new BarkHeader(alice, bob, 0L, Bark.MAX_MESSAGE_SIZE);

        // convert the BarkPacket to encrypted bytes only bob can open
        final byte[] bytes = barkHeader.toEncryptedBytes(Crypto.BOB_KEYPAIR.getPublic());

        // convert the byte[] back to a BarkPacket.
        final BarkHeader convertedBarkHeader = BarkHeader.fromEncryptedBytes(bytes, Crypto.BOB_KEYPAIR.getPrivate());

        // assert that the original BarkPacket is identical to the one we converted.
        assertEquals(barkHeader, convertedBarkHeader);
    }
}
