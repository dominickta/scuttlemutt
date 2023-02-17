package storagemanager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import crypto.Crypto;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.TestUtils;

public class MapStorageManagerTest {

    // runtime-defined test objects
    private Bark b;
    private DawgIdentifier m;
    private Conversation c;
    private SecretKey k1, k2;
    private MapStorageManager mapStorageManager;

    @BeforeEach
    public void setup() {
        b = TestUtils.generateRandomizedBark();
        m = TestUtils.generateRandomizedDawgIdentifier();
        c = TestUtils.generateRandomizedConversation();
        k1 = Crypto.generateSecretKey();
        k2 = Crypto.generateSecretKey();
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
    public void testDawgIdentifierStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeDawgIdentifier(m);

        // lookup the object in the storage manager.
        final DawgIdentifier obtainedDawgIdentifier = this.mapStorageManager.lookupDawgIdentifier(m.getPublicKey());
        assertEquals(m, obtainedDawgIdentifier);

        // successfully delete the object.
        this.mapStorageManager.deleteDawgIdentifier(m.getPublicKey());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupDawgIdentifier(m.getPublicKey()));
    }

    @Test
    public void testConversationStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeConversation(c);

        // lookup the object in the storage manager.
        final Conversation obtainedConversation = this.mapStorageManager
                .lookupConversation(c.getOtherPerson().getPublicKey());
        assertEquals(c, obtainedConversation);

        // successfully delete the object.
        this.mapStorageManager.deleteConversation(c.getOtherPerson().getPublicKey());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupConversation(c.getOtherPerson().getPublicKey()));
    }

    @Test
    public void testKeyStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeSecretKeyForPublicKey(m.getPublicKey(), k1);

        // lookup the object in the storage manager.
        final SecretKey obtainedKey = this.mapStorageManager.lookupSecretKeyForPublicKey(m.getPublicKey());
        assertEquals(k1, obtainedKey);

        // update the key in the storage manager.
        this.mapStorageManager.storeSecretKeyForPublicKey(m.getPublicKey(), k2);

        // lookup the object in the storage manager, verify that it was updated.
        final SecretKey obtainedUpdatedKey = this.mapStorageManager.lookupSecretKeyForPublicKey(m.getPublicKey());
        assertEquals(k2, obtainedUpdatedKey);

        // successfully delete the object.
        this.mapStorageManager.deleteSecretKeyForPublicKey(m.getPublicKey());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupSecretKeyForPublicKey(m.getPublicKey()));
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
