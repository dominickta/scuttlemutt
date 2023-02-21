package androidTest.backendimplementations.storagemanager;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.key.KeyEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.message.MessageEntry;

import org.junit.Test;

import java.security.Key;

import crypto.Crypto;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.TestUtils;
import types.serialization.SerializationUtils;

/**
 * Tests the `*Entry` objects used to represent database entries.
 */
public class EntryTypesTest {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    @Test
    public void testBarkEntryCreation_successfullyCreatesBarkEntry() {
        // create the Bark object used for the entry.
        final Bark b = TestUtils.generateRandomizedBark();

        // create the entry.
        final BarkEntry be = new BarkEntry(b);

        // verify that the contents of the entry are as expected.
        assertEquals(new String(b.toNetworkBytes()), be.barkJson);
        assertEquals(b.getUniqueId().toString(), be.uuid);
    }

    @Test
    public void testConversationEntryCreation_successfullyCreatesConversationEntry() {
        // create the Conversation object used for the entry.
        final Conversation c = TestUtils.generateRandomizedConversation();

        // create the entry.
        final ConversationEntry ce = new ConversationEntry(c);

        // verify that the contents of the entry are as expected.
        assertEquals(new String(c.toNetworkBytes()), ce.conversationJson);
        assertEquals(GSON.toJson(c.getOtherPerson().getUUID()), ce.userIdJson);
    }

    @Test
    public void testDawgIdentifierEntryCreation_successfullyCreatesDawgIdentifierEntry() {
        // create the DawgIdentifier object used for the entry.
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();

        // create the entry.
        final DawgIdentifierEntry de = new DawgIdentifierEntry(d);

        // verify that the contents of the entry are as expected.
        assertEquals(new String(d.toNetworkBytes()), de.dawgIdentifierJson);
        assertEquals(d.getUUID().toString(), de.uuid);
    }

    @Test
    public void testKeyEntryCreation_successfullyCreatesKeyEntry() {
        // create the DawgIdentifier and Key objects used for the entry.
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();
        final Key k = Crypto.DUMMY_SECRETKEY;

        // create the entry.
        final KeyEntry ke = new KeyEntry(d.getUUID().toString(),
                new String(SerializationUtils.serializeKey(k)), "", "");

        // verify that the contents of the entry are as expected.
        assertEquals(new String(SerializationUtils.serializeKey(k)), ke.symmetricKeyListJson);
        assertEquals(d.getUUID().toString(), ke.uuid);
    }

    @Test
    public void testMessageEntryCreation_successfullyCreatesMessageEntry() {
        // create the Message object used for the entry.
        final Message m = TestUtils.generateRandomizedMessage();

        // create the entry.
        final MessageEntry me = new MessageEntry(m);

        // verify that the contents of the entry are as expected.
        assertEquals(new String(m.toNetworkBytes()), me.messageJson);
    }

    @Test
    public void testToBark_successfullyCreatesBarkObject() {
        // create the Bark object used for creating the entry + create the entry.
        final Bark b = TestUtils.generateRandomizedBark();
        final BarkEntry be = new BarkEntry(b);
        
        // call BarkEntry.toBark() to get a Bark object.
        final Bark convertedBark = be.toBark();

        // verify that the original Bark == the converted Bark.
        assertEquals(b, convertedBark);
    }

    @Test
    public void testToConversation_successfullyCreatesConversationObject() {
        // create the Conversation object used for creating the entry + create the entry.
        final Conversation c = TestUtils.generateRandomizedConversation();
        final ConversationEntry ce = new ConversationEntry(c);

        // call ConversationEntry.toConversation() to get a Conversation object.
        final Conversation convertedConversation = ce.toConversation();

        // verify that the original Conversation == the converted Conversation.
        assertEquals(c, convertedConversation);
    }

    @Test
    public void testToDawgIdentifier_successfullyCreatesDawgIdentifierObject() {
        // create the DawgIdentifier object used for creating the entry + create the entry.
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();
        final DawgIdentifierEntry de = new DawgIdentifierEntry(d);

        // call DawgIdentifierEntry.toDawgIdentifier() to get a DawgIdentifier object.
        final DawgIdentifier convertedDawgIdentifier = de.toDawgIdentifier();

        // verify that the original DawgIdentifier == the converted DawgIdentifier.
        assertEquals(d, convertedDawgIdentifier);
    }
}
