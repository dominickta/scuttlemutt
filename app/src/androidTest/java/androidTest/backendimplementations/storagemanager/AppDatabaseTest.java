package androidTest.backendimplementations.storagemanager;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.scuttlemutt.app.backendimplementations.storagemanager.AppDatabase;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkDao;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationDao;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierDao;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierEntry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.TestUtils;

/**
 * Test suite for the AppDatabase class.
 *
 * Since the Daos are pretty darn simple + can only be constructed by the AppDatabase
 * class, this class tests all Dao functionality as well.
 *
 * NOTE:  These tests may intermittently fail--this is expected since Room DB operations execute
 * asynchronously and can cause race conditions to arise.
 *
 * (This pattern follows the advice given in Google's official Room testing tutorial:
 *  <a href="https://developer.android.com/training/data-storage/room/testing-db">...</a>)
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class AppDatabaseTest {
    // this rule attempts to force the threads to execute synchronously for our tests.
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    // test objects
    AppDatabase appDb;
    BarkDao barkDao;
    ConversationDao conversationDao;
    DawgIdentifierDao dawgIdentifierDao;

    @Before
    public void setUp() {
        // build the DB, get references to the Daos.
        final Context context = ApplicationProvider.getApplicationContext();
        this.appDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();;
        this.barkDao = this.appDb.barkDao();
        this.conversationDao = this.appDb.conversationDao();
        this.dawgIdentifierDao = this.appDb.dawgIdentifierDao();
    }

    @After
    public void tearDown() {
        appDb.close();
    }

    @Test
    public void testBarkDaoLifecycle_insertEntry_findEntry_deleteEntry() {
        // create the BarkEntry object used for testing.
        final Bark b = TestUtils.generateRandomizedBark();
        final BarkEntry be = new BarkEntry(b);

        // insert the entry into the database.
        this.barkDao.insertBarkEntry(be);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the entry + verify the returned value matches what we entered.
        final BarkEntry obtainedEntry = this.barkDao.findByUuid(be.uuid);
        assertEquals(be, obtainedEntry);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // delete the entry from the database.
        this.barkDao.deleteBarkEntry(be);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // assert that the entry was deleted.
        assertNull(this.barkDao.findByUuid(be.uuid));
    }

    @Test
    public void testConversationDaoLifecycle_insertEntry_findEntry_deleteEntry() {
        // create the ConversationEntry object used for testing.
        final Conversation c = TestUtils.generateRandomizedConversation();
        final ConversationEntry ce = new ConversationEntry(c);

        // insert the entry into the database.
        this.conversationDao.insertConverationEntry(ce);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the entry + verify the returned value matches what we entered.
        final ConversationEntry obtainedEntry = this.conversationDao.findByUuidList(ce.userUuidListJson);
        assertEquals(ce, obtainedEntry);

        // delete the entry from the database.
        this.conversationDao.deleteConverationEntry(ce);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // assert that the entry was deleted.
        assertNull(this.conversationDao.findByUuidList(ce.userUuidListJson));
    }

    @Test
    public void testDawgIdentifierDaoLifecycle_insertEntry_findEntry_deleteEntry() {
        // create the DawgIdentifierEntry object used for testing.
        final DawgIdentifier d = TestUtils.generateRandomizedDawgIdentifier();
        final DawgIdentifierEntry de = new DawgIdentifierEntry(d);

        // insert the entry into the database.
        this.dawgIdentifierDao.insertDawgIdentifierEntry(de);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // lookup the entry + verify the returned value matches what we entered.
        final DawgIdentifierEntry obtainedEntry = this.dawgIdentifierDao.findByUuid(de.uuid);
        assertEquals(de, obtainedEntry);

        // delete the entry from the database.
        this.dawgIdentifierDao.deleteDawgIdentifierEntry(de);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // assert that the entry was deleted.
        assertNull(this.dawgIdentifierDao.findByUuid(de.uuid));
    }

    @Test
    public void testConversationDaoListAllConversations_insertTwoEntries_successfullyListTwoEntries() {
        // create the ConversationEntry objects used for testing.
        final Conversation c1 = TestUtils.generateRandomizedConversation();
        final ConversationEntry ce1 = new ConversationEntry(c1);
        final Conversation c2 = TestUtils.generateRandomizedConversation();
        final ConversationEntry ce2 = new ConversationEntry(c2);

        // insert the entries into the database.
        this.conversationDao.insertConverationEntry(ce1);
        this.conversationDao.insertConverationEntry(ce2);

        // allow request to complete.
        TestUtils.sleepOneSecond();

        // list the entries from the database.
        final List<ConversationEntry> obtainedEntries = this.conversationDao.listAllConversations();

        // assert that both entries were returned from the database.
        assertTrue(obtainedEntries.contains(ce1));
        assertTrue(obtainedEntries.contains(ce2));
    }
}