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

import java.util.List;

import crypto.Crypto;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.TestUtils;

/**
 * Tests the RoomStorageManager class.
 *
 * NOTE:  These tests may intermittently fail--this is expected since Room DB operations execute
 * asynchronously and can cause race conditions to arise.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class RoomStorageManagerTest {
    // this rule attempts to force the threads to execute synchronously for our tests.
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
    public void testDawgIdentifierStorageLifecycle() {
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();

        // create the object in the storage manager.
        this.storageManager.storeDawgIdentifier(d);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.storageManager.lookupDawgIdentifier(d.getUniqueId());
        assertEquals(d, obtainedDawgIdentifier);

        // update the object in the storage manager.
        d.setPublicKey(Crypto.generateKeyPair().getPublic());
        this.storageManager.storeDawgIdentifier(d);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the updated object was stored by looking it up.
        final DawgIdentifier obtainedUpdatedDawgIdentifier = this.storageManager.lookupDawgIdentifier(d.getUniqueId());
        assertNotEquals(obtainedUpdatedDawgIdentifier.getPublicKey(), obtainedDawgIdentifier.getPublicKey());
        assertEquals(d.getPublicKey(), obtainedUpdatedDawgIdentifier.getPublicKey());

        // successfully delete the object.
        this.storageManager.deleteDawgIdentifier(d.getUniqueId());

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // verify that the object was deleted.
        assertNull(this.storageManager.lookupDawgIdentifier(d.getUniqueId()));
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