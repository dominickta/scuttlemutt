package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class MessageTest {
    // test variables
    private static final String plaintextMessage1 = RandomStringUtils.randomAlphanumeric(15);
    private static final String plaintextMessage2 = RandomStringUtils.randomAlphanumeric(15);
    private static final Long orderNum1 = new Random().nextLong();
    private static final Long orderNum2 = new Random().nextLong();
    private static final DawgIdentifier sender1 = TestUtils.generateRandomizedDawgIdentifier();
    private static final DawgIdentifier sender2 = TestUtils.generateRandomizedDawgIdentifier();

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a Message object.
        final Message m = new Message(plaintextMessage1, orderNum1, sender1);
    }

    @Test
    public void testEquals_differentOrderNumButSameMessageAndSameSender_returnsFalse() {
        // Create two Message objects with the same message but different orderNums.
        final Message m1 = new Message(plaintextMessage1, orderNum1, sender1);
        final Message m2 = new Message(plaintextMessage1, orderNum2, sender1);

        // verify that the two Message objects are not equal.
        assertNotEquals(m1, m2);
    }

    @Test
    public void testEquals_sameOrderNumAndSameSenderButDifferentMessage_returnsFalse() {
        // Create two Message objects with different messages but the same orderNum.
        final Message m1 = new Message(plaintextMessage1, orderNum1, sender1);
        final Message m2 = new Message(plaintextMessage2, orderNum1, sender1);

        // verify that the two Message objects are not equal.
        assertNotEquals(m1, m2);
    }

    @Test
    public void testEquals_sameOrderNumAndSameMessageButDifferentSender_returnsFalse() {
        // Create two Message objects with different messages but the same orderNum.
        final Message m1 = new Message(plaintextMessage1, orderNum1, sender1);
        final Message m2 = new Message(plaintextMessage2, orderNum1, sender2);

        // verify that the two Message objects are not equal.
        assertNotEquals(m1, m2);
    }

    @Test
    public void testEquals_sameOrderNumAndSameMessageAndSameSender_returnsTrue() {
        // Create two Message objects with the same message and orderNums.
        final Message m1 = new Message(plaintextMessage1, orderNum1, sender1);
        final Message m2 = new Message(plaintextMessage1, orderNum1, sender1);

        // verify that the two Message objects are not equal.
        assertEquals(m1, m2);
    }
}
