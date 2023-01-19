package types;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MuttIdentifierTest {
    // test variables
    private static final String userName1 = RandomStringUtils.random(15);
    private static final String userName2 = RandomStringUtils.random(15);
    private static final UUID uuid1 = UUID.randomUUID();
    private static final UUID uuid2 = UUID.randomUUID();
    private static final String publicKey1 = RandomStringUtils.random(15);
    private static final String publicKey2 = RandomStringUtils.random(15);

    @Test
    public void testConstructor_createsObjectSuccessfully(){
        // Successfully create a MuttIdentifier object.
        final MuttIdentifier m = new MuttIdentifier(userName1, uuid1, publicKey1);
    }

    @Test
    public void testEquals_differentUsernameAndKeyButSameUUID_returnsTrue(){
        // Create two MuttIdentifier objects with the same UUID but different usernames.
        final MuttIdentifier m1 = new MuttIdentifier(userName1, uuid1, publicKey1);
        final MuttIdentifier m2 = new MuttIdentifier(userName2, uuid1, publicKey2);


        // verify that the two MuttIdentifier objects are equal.
        assertEquals(m1, m2);
    }

    @Test
    public void testEquals_differentUUID_returnsFalse(){
        // Create two MuttIdentifier objects with the same UUID but different usernames.
        final MuttIdentifier m1 = new MuttIdentifier(userName1, uuid1, publicKey1);
        final MuttIdentifier m2 = new MuttIdentifier(userName2, uuid2, publicKey2);


        // verify that the two MuttIdentifier objects are not equal.
        assertNotEquals(m1, m2);
    }

}
