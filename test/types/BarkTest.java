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
        assertThrows(RuntimeException.class, () -> new Bark(oversizedMessage));
    }

    @Test
    public void testConstructor_contentsSafeSize_createsObjectSuccessfully() {
        // Successfully reate a Bark object with the valid message String.
        final Bark b = new Bark(validMessage);
    }

    @Test
    public void testEquals_sameObject_returnsTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage);

        // verify that it is equal to itself.
        assertEquals(b, b);
    }

    @Test
    public void testCanRetransmit_noRetransmissionsRemaining_returnFalse() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage);

        // decrement the retransmissions such that none are left.
        for (int i = 0; i < Bark.MAX_RETRANSMISSIONS; i++) {
            b.decrementRetransmissionsLeft();
        }

        // assert that no retransmissions remain.
        assertEquals(0, b.getRetransmissionsLeft());

        // assert that canRetransmit() returns false.
        assertFalse(b.canRetransmit());
    }

    @Test
    public void testCanRetransmit_retransmissionsRemaining_returnTrue() {
        // Create a Bark object with the valid message String.
        final Bark b = new Bark(validMessage);

        // decrement the retransmissions such that none are left.
        for (int i = 0; i < Bark.MAX_RETRANSMISSIONS - 1; i++) {
            b.decrementRetransmissionsLeft();
        }

        // assert that no retransmissions remain.
        assertEquals(1, b.getRetransmissionsLeft());

        // assert that canRetransmit() returns false.
        assertTrue(b.canRetransmit());
    }

    @Test
    public void testEquals_sameContentsDifferentObjects_returnsFalse() {
        // Create two Bark objects with the same message String.
        final Bark b1 = new Bark(validMessage);
        final Bark b2 = new Bark(validMessage);

        // verify that they are not equal since they don't have the same unique ID.
        assertNotEquals(b1, b2);
    }

}
