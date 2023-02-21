package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

public class DawgIdentifierTest {
    // test variables
    private static final String userName1 = RandomStringUtils.randomAlphanumeric(15);
    private static final String userName2 = RandomStringUtils.randomAlphanumeric(15);

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a DawgIdentifier object.
        final DawgIdentifier m = new DawgIdentifier(userName1, UUID.randomUUID());
        assertNotNull(m);
    }

    @Test
    public void testEquals_differentUsernameAndKeyButSameUUID_returnsTrue() {
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        UUID id = UUID.randomUUID();
        final DawgIdentifier m1 = new DawgIdentifier(userName1, id);
        final DawgIdentifier m2 = new DawgIdentifier(userName2, id);

        // verify that the two DawgIdentifier objects are equal.
        assertEquals(m1, m2);
    }

    @Test
    public void testEquals_differentUUID_returnsFalse() {
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        final DawgIdentifier m1 = new DawgIdentifier(userName1, UUID.randomUUID());
        final DawgIdentifier m2 = new DawgIdentifier(userName2, UUID.randomUUID());

        // verify that the two DawgIdentifier objects are not equal.
        assertNotEquals(m1, m2);
    }

}
