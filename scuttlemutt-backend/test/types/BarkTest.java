package types;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static types.Bark.MAX_MESSAGE_SIZE;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import crypto.Crypto;

public class BarkTest {
    // test variables
    private static final String oversizedMessage = RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE + 1);
    private static final String validMessage = RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE);

    @Test
    public void testConstructor_contentsTooLarge_throwsRuntimeException() {
        // attempt to create a Bark object with the invalid message String and assert that the RuntimeException is thrown.
        assertThrows(RuntimeException.class, () -> new Bark(oversizedMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey()
        ));
    }

    @Test
    public void testConstructor_contentsSafeSize_createsObjectSuccessfully() {
        // Successfully create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey()
        );
    }

    @Test
    public void testGetContents_keyListContainsValidKey_returnsMessageSuccessfully() {
        final SecretKey validKey = Crypto.generateSecretKey();

        // create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                validKey
        );

        // create the keyList.  To make things a bit more difficult, make validKey not the last object.
        // (this means that getContents() needs to iterate to the correct Key.)
        final SecretKey otherKey = Crypto.generateSecretKey();
        final List<Key> keyList = new ArrayList<Key>();
        keyList.add(validKey);
        keyList.add(otherKey);

        // call getContents().
        final String contents = b.getContents(keyList).get();
        assertEquals(validMessage, contents);
    }

    @Test
    public void testGetContents_noValidKeyInList_returnsOptionalEmpty() {

        // create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.DUMMY_SECRETKEY
        );

        // create the keyList.  To make things a bit more difficult, make validKey not the last object.
        // (this means that getContents() needs to iterate to the correct Key.)
        final SecretKey invalidKey = Crypto.generateSecretKey();
        final List<Key> keyList = new ArrayList<Key>();
        keyList.add(invalidKey);

        // call getContents().
        final Optional<String> contents = b.getContents(keyList);

        // assert that we were unable to successfully decrypt the contents.
        assertFalse(contents.isPresent());
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey()
        );

        // verify that it is equal to itself.
        assertEquals(b, b);
    }

    @Test
    public void testEquals_sameContentsDifferentObjects_returnsFalse() {
        // Create two Bark objects with the same message String.
        final Bark b1 = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L,
                Crypto.generateSecretKey()
        );
        final Bark b2 = new Bark(validMessage,
                b1.getSender(),
                b1.getReceiver(),
                0L,
                Crypto.generateSecretKey()
        );

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
                Crypto.generateSecretKey()
        );

        // Convert the Bark object to a byte[] for sending over the network.
        final byte[] byteArray = b.toNetworkBytes();

        // Convert the byteArray back to a Bark object.
        final Bark rebuiltBark = Bark.fromNetworkBytes(byteArray);

        // Verify that they're equal.
        assertEquals(b, rebuiltBark);
    }
}
