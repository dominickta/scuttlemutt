package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static types.Bark.MAX_MESSAGE_SIZE;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class BarkTest {
    // test variables
    private static final String oversizedMessage = RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE + 1);
    private static final String validMessage = RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE);

    @Test
    public void testConstructor_contentsTooLarge_throwsRuntimeException() {
        // attempt to create a Bark object with the invalid message String and assert
        // that the RuntimeException is thrown.
        assertThrows(RuntimeException.class, () -> new Bark(oversizedMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey()));
    }

    @Test
    public void testConstructor_contentsSafeSize_createsObjectSuccessfully() {
        // Successfully create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey());
        assertNotNull(b);
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey());

        // verify that it is equal to itself.
        assertEquals(b, b);
    }

    @Test
    public void testEquals_sameContentsDifferentObjects_returnsFalse() {
        // Create two Bark objects with the same message String.
        DawgIdentifier alice = TestUtils.generateRandomizedDawgIdentifier();
        DawgIdentifier bob = TestUtils.generateRandomizedDawgIdentifier();
        final Bark b1 = new Bark(validMessage,
                alice,
                bob,
                0L,
                Crypto.generateSecretKey());
        final Bark b2 = new Bark(validMessage,
                alice,
                bob,
                0L,
                Crypto.generateSecretKey());

        // verify that they are not equal since they don't have the same unique ID.
        assertNotEquals(b1, b2);
    }

    @Test
    public void testNetworkByteConversion_convertsToBytes_convertsFromBytes_identicalObject() {
        // Create a Bark object for the test.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey());

        // Convert the Bark object to a byte[] for sending over the network.
        final byte[] byteArray = b.toNetworkBytes();

        // Convert the byteArray back to a Bark object.
        final Bark rebuiltBark = Bark.fromNetworkBytes(byteArray);

        // Verify that they're equal.
        assertEquals(b, rebuiltBark);
    }

    @Test
    public void testIsForMe_BarkFromAliceToBob_isForBob_returnsTrue() {
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic()),
                new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic()),
                0L,
                Crypto.generateSecretKey());
        assertTrue(b.isForMe(Crypto.BOB_KEYPAIR.getPrivate()));
    }

    @Test
    public void testIsForMe_BarkFromAliceToBob_isForAlice_returnsFalse() {
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic()),
                new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic()),
                0L,
                Crypto.generateSecretKey());
        assertFalse(b.isForMe(Crypto.ALICE_KEYPAIR.getPrivate()));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetsContents() {
        // alice sends message to bob, only bob can getContents
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic()),
                new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic()),
                0L,
                secret);
        assertEquals(validMessage, b.getContents(Crypto.BOB_KEYPAIR.getPrivate(), secret));
    }

    @Test
    public void testNotEquals_BarkFromAliceToBob_aliceGetsContents() {
        // alice sends message to bob, only bob can getContents
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic()),
                new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic()),
                0L,
                secret);
        assertNotEquals(validMessage, b.getContents(Crypto.ALICE_KEYPAIR.getPrivate(), secret));
        assertNull(b.getContents(Crypto.ALICE_KEYPAIR.getPrivate(), secret));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetSender() {
        // alice sends message to bob, only bob can getSender
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertEquals(alice, b.getSender(Crypto.BOB_KEYPAIR.getPrivate()));
    }

    @Test
    public void testNotEquals_BarkFromAliceToBob_aliceGetSender() {
        // alice sends message to bob, only bob can getSender
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertNotEquals(alice, b.getSender(Crypto.ALICE_KEYPAIR.getPrivate()));
        assertNull(b.getSender(Crypto.ALICE_KEYPAIR.getPrivate()));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetReceiver() {
        // alice sends message to bob, only bob can getReceiver
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertEquals(bob, b.getReceiver(Crypto.BOB_KEYPAIR.getPrivate()));
    }

    @Test
    public void testNotEquals_BarkFromAliceToBob_aliceGetReceiver() {
        // alice sends message to bob, only bob can getReceiver
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertNotEquals(bob, b.getReceiver(Crypto.ALICE_KEYPAIR.getPrivate()));
        assertNull(b.getReceiver(Crypto.ALICE_KEYPAIR.getPrivate()));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetOrderNum() {
        // alice sends message to bob, only bob can getOrderNum
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertEquals(bob, b.getOrderNum(Crypto.BOB_KEYPAIR.getPrivate()));
    }

    @Test
    public void testNotEquals_BarkFromAliceToBob_aliceGetOrderNum() {
        // alice sends message to bob, only bob can getOrderNum
        DawgIdentifier alice = new DawgIdentifier("alice", Crypto.ALICE_KEYPAIR.getPublic());
        DawgIdentifier bob = new DawgIdentifier("bob", Crypto.BOB_KEYPAIR.getPublic());
        SecretKey secret = Crypto.generateSecretKey();
        final Bark b = new Bark(validMessage, alice, bob, 0L, secret);
        assertNotEquals(bob, b.getOrderNum(Crypto.ALICE_KEYPAIR.getPrivate()));
        assertNull(b.getOrderNum(Crypto.ALICE_KEYPAIR.getPrivate()));
    }
}
