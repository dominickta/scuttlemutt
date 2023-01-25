package types;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ConversationTest {
    // test variables
    private List<MuttIdentifier> userList1;
    private List<MuttIdentifier> userList2;

    @BeforeEach
    public void setup() {
        // create a Supplier which generates randomized MuttIdentifiers.
        final Supplier<MuttIdentifier> muttIdentifierSupplier = () -> {
            final String userName = RandomStringUtils.randomAlphanumeric(15);
            final UUID uuid = UUID.randomUUID();
            final String publicKey = RandomStringUtils.randomAlphanumeric(15);
            return new MuttIdentifier(userName, uuid, publicKey);
        };

        // create the userLists using the Supplier.
        userList1 = Stream.generate(muttIdentifierSupplier)
                .limit(15)
                .toList();
        userList2 = Stream.generate(muttIdentifierSupplier)
                .limit(15)
                .toList();
    }

    @Test
    public void testConstructor_createsObjectSuccessfully(){
        // Successfully create a Conversation object.
        final Conversation c = new Conversation(userList1);
    }

    @Test
    public void testGetUserUUIDList_returnsCorrectList(){
        // Successfully create a Conversation object.
        final Conversation c = new Conversation(userList1);

        // Get the UUID List from the method.
        final List<UUID> uuidList = c.getUserUUIDList();

        // Verify that all UUIDs are present in the UUID List.
        for (MuttIdentifier m : userList1) {
            assertTrue(uuidList.contains(m.getUniqueId()));
        }
    }

    @Test
    public void testEquals_differentObjectsButSameList_returnsTrue(){
        // Create two Conversation objects with the same List.
        final Conversation c1 = new Conversation(userList1);
        final Conversation c2 = new Conversation(userList1);


        // Verify that the two Conversation objects are equal.
        assertEquals(c1, c2);
    }

    @Test
    public void testEquals_differentLists_returnsFalse(){
        // Create two Conversation objects with the same List.
        final Conversation c1 = new Conversation(userList1);
        final Conversation c2 = new Conversation(userList2);


        // Verify that the two Conversation objects are not equal.
        assertNotEquals(c1, c2);
    }

}
