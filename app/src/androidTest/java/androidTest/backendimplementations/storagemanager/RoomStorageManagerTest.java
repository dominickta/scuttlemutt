package androidTest.backendimplementations.storagemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.scuttlemutt.app.backendimplementations.storagemanager.AppDatabase;
import com.scuttlemutt.app.backendimplementations.storagemanager.RoomStorageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

import crypto.Crypto;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.TestUtils;

/**
 * Tests the RoomStorageManager class.
 *
 * NOTE: These tests may intermittently fail--this is expected since Room DB
 * operations execute
 * asynchronously and can cause race conditions to arise.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RoomStorageManagerTest {
    // this rule attempts to force the threads to execute synchronously for our
    // tests.
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    AppDatabase appDb;
    RoomStorageManager storageManager;

    @Before
    public void setUp() {
        // create the database + RoomStorageManager.
        final Context context = ApplicationProvider.getApplicationContext();
        this.appDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        this.storageManager = new RoomStorageManager(this.appDb);
    }

    @After
    public void tearDown() {
        this.appDb.close();
    }

    @Test
    public void testBarkStorageLifecycle() {
        final Bark b = TestUtils.generateRandomizedBark();

        // create the object in the storage manager.
        this.storageManager.storeBark(b);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final Bark obtainedBark = this.storageManager.lookupBark(b.getUniqueId());
        assertEquals(b, obtainedBark);

        // successfully delete the object.
        this.storageManager.deleteBark(b.getUniqueId());

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupBark(b.getUniqueId()));
    }

    @Test
    public void testDawgIdentifierStorageLifecycleUuidMethods() {
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();

        // create the object in the storage manager.
        this.storageManager.storeDawgIdentifier(d);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.storageManager.lookupDawgIdentifierForUuid(d.getUniqueId());
        assertEquals(d, obtainedDawgIdentifier);

        // successfully delete the object.
        this.storageManager.deleteDawgIdentifierByUuid(d.getUniqueId());

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupDawgIdentifierForUuid(d.getUniqueId()));
    }

    @Test
    public void testDawgIdentifierStorageLifecycleUserContactMethods() {
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();

        // create the object in the storage manager.
        this.storageManager.storeDawgIdentifier(d);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.storageManager.lookupDawgIdentifierForUserContact(d.getUserContact());
        assertEquals(d, obtainedDawgIdentifier);

        // successfully delete the object.
        this.storageManager.deleteDawgIdentifierByUserContact(d.getUserContact());

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupDawgIdentifierForUserContact(d.getUserContact()));
    }


    @Test
    public void testConversationStorageLifecycle() {
        final Conversation c = TestUtils.generateRandomizedConversation();

        // create the object in the storage manager.
        this.storageManager.storeConversation(c);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final Conversation obtainedConversation = this.storageManager.lookupConversation(c.getUserUUIDList());
        assertEquals(c, obtainedConversation);

        // successfully delete the object.
        this.storageManager.deleteConversation(c.getUserUUIDList());

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupConversation(c.getUserUUIDList()));
    }

    @Test
    public void testKeyStorageLifecycle() {
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();

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
            this.storageManager.storeKeyForDawgIdentifier(d.getUniqueId(), currentKey);

            // verify that the Key was successfully stored in the List.
            final List<Key> obtainedKeys = this.storageManager.lookupKeysForDawgIdentifier(d.getUniqueId());
            assertEquals(currentKey, obtainedKeys.get(obtainedKeys.size() - 1));
            assertEquals(i + 1, obtainedKeys.size());  // assert the List contains all Keys added so far.
        }

        // verify that the oldest key is removed from the stored List when we add Keys after hitting
        // the size limit.
        final Key extraKey = Crypto.generateSecretKey();
        this.storageManager.storeKeyForDawgIdentifier(d.getUniqueId(), extraKey);
        final List<Key> obtainedKeys = this.storageManager.lookupKeysForDawgIdentifier(d.getUniqueId());

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
        this.storageManager.deleteKeysForDawgIdentifier(d.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupKeysForDawgIdentifier(d.getUniqueId()));
    }

    @Test
    public void testMessageStorageLifecycle() {
        final Message m = TestUtils.generateRandomizedMessage();

        // create the object in the storage manager.
        this.storageManager.storeMessage(m);

        // lookup the object in the storage manager.
        final Message obtainedMessage = this.storageManager.lookupMessage(m.getUniqueId());
        assertEquals(m, obtainedMessage);

        // successfully delete the object.
        this.storageManager.deleteMessage(m.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupMessage(m.getUniqueId()));
    }

    @Test
    public void testListConversations_allConversationsAreListed() {
        final Conversation c = TestUtils.generateRandomizedConversation();

        // create the object in the storage manager.
        this.storageManager.storeConversation(c);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final List<Conversation> listedConversations = this.storageManager.listAllConversations();
        assertEquals(1, listedConversations.size());
        assertEquals(c, listedConversations.get(0));
    }
}