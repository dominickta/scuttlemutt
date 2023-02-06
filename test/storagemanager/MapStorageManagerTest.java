package storagemanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.TestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class MapStorageManagerTest {
    
    // runtime-defined test objects
    private Bark b;
    private DawgIdentifier m;
    private Conversation c;
    private MapStorageManager mapStorageManager;
    
    @BeforeEach
    public void setup() {
        b = TestUtils.generateRandomizedBark();
        m = TestUtils.generateRandomizedDawgIdentifier();
        c = TestUtils.generateRandomizedConversation();
        this.mapStorageManager = new MapStorageManager();
    }

    @Test
    public void testDawgIdentifierStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeDawgIdentifier(m);

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.mapStorageManager.lookupDawgIdentifier(m.getUniqueId());
        assertEquals(m, obtainedDawgIdentifier);

        // update the object in the storage manager.
        m.setPublicKey(RandomStringUtils.randomAlphanumeric(15));
        this.mapStorageManager.storeDawgIdentifier(m);

        // verify that the updated object was stored by looking it up.
        final DawgIdentifier obtainedUpdatedDawgIdentifier = this.mapStorageManager.lookupDawgIdentifier(m.getUniqueId());
        assertNotEquals(obtainedUpdatedDawgIdentifier.getPublicKey(), obtainedDawgIdentifier.getPublicKey());
        assertEquals(m.getPublicKey(), obtainedUpdatedDawgIdentifier.getPublicKey());

        // successfully delete the object.
        this.mapStorageManager.deleteDawgIdentifier(m.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupDawgIdentifier(m.getUniqueId()));
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
    public void testListConversations_allConversationsAreListed() {
        // create the object in the storage manager.
        this.mapStorageManager.storeConversation(c);

        // lookup the object in the storage manager.
        final Conversation obtainedConversation = this.mapStorageManager.listAllConversations().get(0);
        assertEquals(c, obtainedConversation);
    }
}
