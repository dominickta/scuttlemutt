package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static types.Bark.MAX_MESSAGE_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY));
    }

    @Test
    public void testConstructor_contentsSafeSize_createsObjectSuccessfully() {
        // Successfully create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);
        assertNotNull(b);
    }

    @Test
    public void testGetContents_keyListContainsValidKey_returnsMessageSuccessfully() {
        final SecretKey validKey = Crypto.DUMMY_SECRETKEY;

        // create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.ALICE_KEYPAIR.getPublic(),
                validKey);

        // create the keyList. To make things a bit more difficult, make validKey not
        // the last object. (this means that getContents() needs to iterate to the
        // correct Key.)
        final SecretKey otherKey = Crypto.generateSecretKey();
        final List<SecretKey> keyList = new ArrayList<>();
        keyList.add(validKey);
        keyList.add(otherKey);

        // call getContents().
        final String contents = b.getContents(keyList);
        assertEquals(validMessage, contents);
    }

    @Test
    public void testGetContents_noValidKeyInList_throws() {
        // create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.ALICE_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);

        // create the keyList. To make things a bit more difficult, make validKey not
        // the last object.
        // (this means that getContents() needs to iterate to the correct Key.)
        final SecretKey invalidKey = Crypto.generateSecretKey();
        final List<SecretKey> keyList = new ArrayList<>();
        keyList.add(invalidKey);

        // assert that we were unable to successfully decrypt the contents.
        assertThrows(RuntimeException.class, () -> b.getContents(keyList));
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);

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
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);
        final Bark b2 = new Bark(validMessage,
                alice,
                bob,
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);

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
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);

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
                new DawgIdentifier("alice", UUID.randomUUID()),
                new DawgIdentifier("bob", UUID.randomUUID()),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);
        assertTrue(b.isForMe(Crypto.BOB_KEYPAIR.getPrivate()));
    }

    @Test
    public void testIsForMe_BarkFromAliceToBob_isForAlice_returnsFalse() {
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", UUID.randomUUID()),
                new DawgIdentifier("bob", UUID.randomUUID()),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);
        assertFalse(b.isForMe(Crypto.ALICE_KEYPAIR.getPrivate()));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetsContents() {
        // alice sends message to bob, only bob can getContents
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", UUID.randomUUID()),
                new DawgIdentifier("bob", UUID.randomUUID()),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                secret);
        assertEquals(validMessage, b.getContents(List.of(secret)));
    }

    @Test
    public void testThrows_BarkFromAliceToBob_aliceGetsContents() {
        // alice sends message to bob, only bob can getContents
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage,
                new DawgIdentifier("alice", UUID.randomUUID()),
                new DawgIdentifier("bob", UUID.randomUUID()),
                0L,
                Crypto.BOB_KEYPAIR.getPublic(),
                secret);
        assertThrows(RuntimeException.class, () -> b.getContents(List.of(Crypto.OTHER_SECRETKEY)));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetSender() {
        // alice sends message to bob, only bob can getSender
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertEquals(alice, b.getSender(List.of(Crypto.DUMMY_SECRETKEY)));
    }

    @Test
    public void testThrows_BarkFromAliceToBob_aliceGetSender() {
        // alice sends message to bob, only bob can getSender
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertThrows(RuntimeException.class, () -> b.getSender(List.of(Crypto.OTHER_SECRETKEY)));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetReceiver() {
        // alice sends message to bob, only bob can getReceiver
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertEquals(bob, b.getReceiver(List.of(Crypto.DUMMY_SECRETKEY)));
    }

    @Test
    public void testThrows_BarkFromAliceToBob_aliceGetReceiver() {
        // alice sends message to bob, only bob can getReceiver
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertThrows(RuntimeException.class, () -> b.getReceiver(List.of(Crypto.OTHER_SECRETKEY)));
    }

    @Test
    public void testEquals_BarkFromAliceToBob_bobGetOrderNum() {
        // alice sends message to bob, only bob can getOrderNum
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertEquals(0L, b.getOrderNum(List.of(Crypto.DUMMY_SECRETKEY)));
    }

    @Test
    public void testThrows_BarkFromAliceToBob_aliceGetOrderNum() {
        // alice sends message to bob, only bob can getOrderNum
        DawgIdentifier alice = new DawgIdentifier("alice", UUID.randomUUID());
        DawgIdentifier bob = new DawgIdentifier("bob", UUID.randomUUID());
        SecretKey secret = Crypto.DUMMY_SECRETKEY;
        final Bark b = new Bark(validMessage, alice, bob, 0L, Crypto.BOB_KEYPAIR.getPublic(), secret);
        assertThrows(RuntimeException.class, () -> b.getOrderNum(List.of(Crypto.OTHER_SECRETKEY)));
    }
}
