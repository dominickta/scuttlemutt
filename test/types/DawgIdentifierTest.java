package types;

import crypto.Crypto;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DawgIdentifierTest {
    // test variables
    private static final String userName1 = RandomStringUtils.randomAlphanumeric(15);
    private static final String userName2 = RandomStringUtils.randomAlphanumeric(15);
    private static final UUID uuid1 = UUID.randomUUID();
    private static final UUID uuid2 = UUID.randomUUID();
    private static final PublicKey publicKey1 = Crypto.generateKeyPair().getPublic();
    private static final PublicKey publicKey2 = Crypto.generateKeyPair().getPublic();

    @Test
    public void testConstructor_createsObjectSuccessfully(){
        // Successfully create a DawgIdentifier object.
        final DawgIdentifier m = new DawgIdentifier(userName1, uuid1, publicKey1);
    }

    @Test
    public void testEquals_differentUsernameAndKeyButSameUUID_returnsTrue(){
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        final DawgIdentifier m1 = new DawgIdentifier(userName1, uuid1, publicKey1);
        final DawgIdentifier m2 = new DawgIdentifier(userName2, uuid1, publicKey2);


        // verify that the two DawgIdentifier objects are equal.
        assertEquals(m1, m2);
    }

    @Test
    public void testEquals_differentUUID_returnsFalse(){
        // Create two DawgIdentifier objects with the same UUID but different usernames.
        final DawgIdentifier m1 = new DawgIdentifier(userName1, uuid1, publicKey1);
        final DawgIdentifier m2 = new DawgIdentifier(userName2, uuid2, publicKey2);


        // verify that the two DawgIdentifier objects are not equal.
        assertNotEquals(m1, m2);
    }

}
