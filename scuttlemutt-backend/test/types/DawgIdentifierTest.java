package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.PublicKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class DawgIdentifierTest {
    // test variables
    private static final String userName1 = RandomStringUtils.randomAlphanumeric(15);
    private static final String userName2 = RandomStringUtils.randomAlphanumeric(15);
    private static final PublicKey pubKey1 = Crypto.ALICE_KEYPAIR.getPublic();
    private static final PublicKey pubKey2 = Crypto.BOB_KEYPAIR.getPublic();

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a DawgIdentifier object.
        final DawgIdentifier m = new DawgIdentifier(userName1, pubKey1);
        assertNotNull(m);
    }

    @Test
    public void testEquals_differentUsernameAndKeyButSameUUID_returnsTrue() {
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        final DawgIdentifier m1 = new DawgIdentifier(userName1, pubKey1);
        final DawgIdentifier m2 = new DawgIdentifier(userName2, pubKey2);

        // verify that the two DawgIdentifier objects are equal.
        assertEquals(m1, m2);
    }

    @Test
    public void testEquals_differentUUID_returnsFalse() {
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        final DawgIdentifier m1 = new DawgIdentifier(userName1, pubKey1);
        final DawgIdentifier m2 = new DawgIdentifier(userName2, pubKey2);

        // verify that the two DawgIdentifier objects are not equal.
        assertNotEquals(m1, m2);
    }

}
