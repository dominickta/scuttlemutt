package storagemanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import crypto.Crypto;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.TestUtils;

public class MapStorageManagerTest {

    // runtime-defined test objects
    private Bark b;
    private DawgIdentifier d;
    private Conversation c;
    private Message m;
    private MapStorageManager mapStorageManager;

    @BeforeEach
    public void setup() {
        b = TestUtils.generateRandomizedBark();
        d = TestUtils.generateRandomizedDawgIdentifier();
        c = TestUtils.generateRandomizedConversation();
        m = TestUtils.generateRandomizedMessage();
        this.mapStorageManager = new MapStorageManager();
    }

    @Test
    public void testBarkStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeBark(b);

        // lookup the object in the storage manager.
        final Bark obtainedBark = this.mapStorageManager.lookupBark(b.getUniqueId());
        assertEquals(b, obtainedBark);

        // successfully delete the object.
        this.mapStorageManager.deleteBark(b.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupBark(b.getUniqueId()));
    }

    @Test
    public void testDawgIdentifierStorageLifecycleUuidMethods() {
        // create the object in the storage manager.
        this.mapStorageManager.storeDawgIdentifier(d);

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.mapStorageManager.lookupDawgIdentifierForUuid(d.getUniqueId());
        assertEquals(d, obtainedDawgIdentifier);

        // successfully delete the object.
        this.mapStorageManager.deleteDawgIdentifierByUuid(d.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupDawgIdentifierForUuid(d.getUniqueId()));
    }

    @Test
    public void testDawgIdentifierStorageLifecycleUserContactMethods() {
        // create the object in the storage manager.
        this.mapStorageManager.storeDawgIdentifier(d);

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.mapStorageManager.lookupDawgIdentifierForUserContact(d.getUserContact());
        assertEquals(d, obtainedDawgIdentifier);

        // successfully delete the object.
        this.mapStorageManager.deleteDawgIdentifierByUserContact(d.getUserContact());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupDawgIdentifierForUserContact(d.getUserContact()));
    }


    @Test
    public void testConversationStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeConversation(c);

        // lookup the object in the storage manager.
        final Conversation obtainedConversation = this.mapStorageManager.lookupConversation(c.getUserUUIDList());
        assertEquals(c, obtainedConversation);

        // successfully delete the object.
        this.mapStorageManager.deleteConversation(c.getUserUUIDList());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupConversation(c.getUserUUIDList()));
    }

    @Test
    public void testKeyStorageLifecycle() {
        // create a List of Key objects to store in the storage manager.
        // we want to store the maximum allowed number of Keys.
        final List<Key> keyList = new ArrayList<Key>();
        for (int i = 0; i < StorageManager.MAX_NUM_HISTORICAL_KEYS_TO_STORE; i++) {
            keyList.add(Crypto.generateSecretKey());
        }

        // store the Key objects one-by-one in the storage manager and verify that the stored
        // List<Key> is updated along the way.
        for (int i = 0; i < StorageManager.MAX_NUM_HISTORICAL_KEYS_TO_STORE; i++) {
            final Key currentKey = keyList.get(i);

            // store the currentKey.
            this.mapStorageManager.storeKeyForDawgIdentifier(d.getUniqueId(), currentKey);

            // verify that the Key was successfully stored in the List.
            final List<Key> obtainedKeys = this.mapStorageManager.lookupKeysForDawgIdentifier(d.getUniqueId());
            assertEquals(currentKey, obtainedKeys.get(obtainedKeys.size() - 1));
            assertEquals(i + 1, obtainedKeys.size());  // assert the List contains all Keys added so far.
        }

        // verify that the oldest key is removed from the stored List when we add Keys after hitting
        // the size limit.
        final Key extraKey = Crypto.generateSecretKey();
        this.mapStorageManager.storeKeyForDawgIdentifier(d.getUniqueId(), extraKey);
        final List<Key> obtainedKeys = this.mapStorageManager.lookupKeysForDawgIdentifier(d.getUniqueId());

        // assert that obtainedKeys == the maximum number of Keys we allow to be stored.
        assertEquals(StorageManager.MAX_NUM_HISTORICAL_KEYS_TO_STORE, obtainedKeys.size());

        // assert that the returned List is as follows:
        // - contains all contents of the original List except the first element, with each element
        //   shifted down one index.
        // - the extraKey is appended to the end of the List.
        final List<Key> expectedKeyList = new ArrayList<Key>(keyList);
        expectedKeyList.remove(0);
        expectedKeyList.add(extraKey);
        assertEquals(expectedKeyList, obtainedKeys);


        // successfully delete the object.
        this.mapStorageManager.deleteKeysForDawgIdentifier(d.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupKeysForDawgIdentifier(d.getUniqueId()));
    }

    @Test
    public void testMessageStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeMessage(m);

        // lookup the object in the storage manager.
        final Message obtainedMessage = this.mapStorageManager.lookupMessage(m.getUniqueId());
        assertEquals(m, obtainedMessage);

        // successfully delete the object.
        this.mapStorageManager.deleteMessage(m.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupMessage(m.getUniqueId()));
    }

    @Test
    public void testListConversations_allConversationsAreListed() {
        // create the object in the storage manager.
        this.mapStorageManager.storeConversation(c);

        // lookup the object in the storage manager.
        final Conversation obtainedConversation = this.mapStorageManager.listAllConversations().get(0);
        assertEquals(c, obtainedConversation);
    }
}
