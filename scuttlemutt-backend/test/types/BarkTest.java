package types;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static types.Bark.MAX_MESSAGE_SIZE;

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
                0L));
    }

    @Test
    public void testConstructor_contentsSafeSize_createsObjectSuccessfully() {
        // Successfully reate a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L);
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L);

        // verify that it is equal to itself.
        assertEquals(b, b);
    }

    @Test
    public void testEquals_sameContentsDifferentObjects_returnsFalse() {
        // Create two Bark objects with the same message String.
        final Bark b1 = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L);
        final Bark b2 = new Bark(validMessage,
                b1.getSender(),
                b1.getReceiver(),
                0L);

        // verify that they are not equal since they don't have the same unique ID.
        assertNotEquals(b1, b2);
    }

    @Test
    public void testNetworkByteConversion_convertsToBytes_convertsFromBytes_identicalObject() {
        // Create a Bark object for the test.
        final Bark b = new Bark(validMessage,
                TestUtils.generateRandomizedDawgIdentifier(),
                TestUtils.generateRandomizedDawgIdentifier(),
                0L);

        // Convert the Bark object to a byte[] for sending over the network.
        final byte[] byteArray = b.toNetworkBytes();

        // Convert the byteArray back to a Bark object.
        final Bark rebuiltBark = Bark.fromNetworkBytes(byteArray);

        // Verify that they're equal.
        assertEquals(b, rebuiltBark);
    }
}
