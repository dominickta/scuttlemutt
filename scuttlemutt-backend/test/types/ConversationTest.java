package types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import crypto.Crypto;

public class ConversationTest {
    // test variables
    private DawgIdentifier user1 = new DawgIdentifier(RandomStringUtils.random(15), Crypto.ALICE_KEYPAIR.getPublic());;
    private DawgIdentifier user2 = new DawgIdentifier(RandomStringUtils.random(15), Crypto.ALICE_KEYPAIR.getPublic());;

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a Conversation object.
        final Conversation c = new Conversation(user1);
        assertNotNull(c);
    }

    @Test
    public void testGetOtherPerson_returnsCorrectPerson() {
        // Successfully create a Conversation object.
        final Conversation c = new Conversation(user1);

        // Get the UUID List from the method.
        final DawgIdentifier otherPerson = c.getOtherPerson();

        // Verify that the person is who we thought they were
        assertEquals(otherPerson, user1);
    }

    @Test
    public void testEquals_differentObjectsButSameList_returnsTrue() {
        // Create two Conversation objects with the same List.
        final Conversation c1 = new Conversation(user1);
        final Conversation c2 = new Conversation(user1);

        // Verify that the two Conversation objects are equal.
        assertEquals(c1, c2);
    }

    @Test
    public void testEquals_differentLists_returnsFalse() {
        // Create two Conversation objects with the same List.
        final Conversation c1 = new Conversation(user1);
        final Conversation c2 = new Conversation(user2);

        // Verify that the two Conversation objects are not equal.
        assertNotEquals(c1, c2);
    }

    @Test
    public void testBarkListContructor_storesBarkUUIDs() {
        // construct a List of Barks.
        final Supplier<Bark> barkSupplier = TestUtils::generateRandomizedBark;
        final List<UUID> barkUUIDs = Stream.generate(barkSupplier)
                .map(Bark::getUniqueId).limit(10)
                .collect(Collectors.toList());

        // construct the Conversation object.
        final Conversation conversationWithBarks = new Conversation(user1, barkUUIDs);

        // verify that the Bark UUIDs were successfully stored in the Conversation.
        assertEquals(barkUUIDs, conversationWithBarks.getBarkUUIDList());
    }

    @Test
    public void testStoreBark_successfullyStoresBark() {
        // create Conversation object with empty Bark UUID List.
        final Conversation c = new Conversation((user1));

        // create a new Bark.
        final Bark b = TestUtils.generateRandomizedBark();

        // store a new Bark in the Conversation object.
        c.storeBarkUUID(b.getUniqueId());

        // assert that the Bark was successfully stored.
        assertTrue(c.getBarkUUIDList().contains(b.getUniqueId()));
    }

}
