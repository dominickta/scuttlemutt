package storagemanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.Bark;
import types.ConversationInfo;
import types.MuttIdentifier;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static types.Bark.MAX_MESSAGE_SIZE;

public class MapStorageManagerTest {
    
    // runtime-defined test objects
    private Bark b;
    private MuttIdentifier m;
    private ConversationInfo c;
    private MapStorageManager mapStorageManager;
    
    @BeforeEach
    public void setup() {
        b = new Bark(RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE));
        m = new MuttIdentifier(RandomStringUtils.randomAlphanumeric(15),
                UUID.randomUUID(),
                RandomStringUtils.randomAlphanumeric(15));
        c = new ConversationInfo(Collections.singletonList(m));
        this.mapStorageManager = new MapStorageManager();
    }
    
    @Test
    public void testBarkStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeBark(b);
        
        // lookup the object in the storage manager.
        final Bark obtainedBark = this.mapStorageManager.lookupBark(b.getUniqueId());
        assertEquals(b, obtainedBark);
        
        // update the object in the storage manager.
        b.decrementRetransmissionsLeft();
        this.mapStorageManager.storeBark(b);
        
        // verify that the updated object was stored by looking it up.
        final Bark obtainedUpdatedBark = this.mapStorageManager.lookupBark(b.getUniqueId());
        assertNotEquals(obtainedUpdatedBark.getRetransmissionsLeft(), obtainedBark.getRetransmissionsLeft());
        assertEquals(b.getRetransmissionsLeft(), obtainedUpdatedBark.getRetransmissionsLeft());
        
        // successfully delete the object.
        this.mapStorageManager.deleteBark(b.getUniqueId());
        
        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupBark(b.getUniqueId()));
    }

    @Test
    public void testMuttIdentifierStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeMuttIdentifier(m);

        // lookup the object in the storage manager.
        final MuttIdentifier obtainedMuttIdentifier = this.mapStorageManager.lookupMuttIdentifier(m.getUniqueId());
        assertEquals(m, obtainedMuttIdentifier);

        // update the object in the storage manager.
        m.setPublicKey(RandomStringUtils.randomAlphanumeric(15));
        this.mapStorageManager.storeMuttIdentifier(m);

        // verify that the updated object was stored by looking it up.
        final MuttIdentifier obtainedUpdatedMuttIdentifier = this.mapStorageManager.lookupMuttIdentifier(m.getUniqueId());
        assertNotEquals(obtainedUpdatedMuttIdentifier.getPublicKey(), obtainedMuttIdentifier.getPublicKey());
        assertEquals(m.getPublicKey(), obtainedUpdatedMuttIdentifier.getPublicKey());

        // successfully delete the object.
        this.mapStorageManager.deleteMuttIdentifier(m.getUniqueId());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupMuttIdentifier(m.getUniqueId()));
    }

    @Test
    public void testConversationInfoStorageLifecycle() {
        // create the object in the storage manager.
        this.mapStorageManager.storeConversationInfo(c);

        // lookup the object in the storage manager.
        final ConversationInfo obtainedConversationInfo = this.mapStorageManager.lookupConversationInfo(c.getUserUUIDList());
        assertEquals(c, obtainedConversationInfo);

        // successfully delete the object.
        this.mapStorageManager.deleteConversationInfo(c.getUserUUIDList());

        // verify that the object was deleted.
        assertNull(this.mapStorageManager.lookupConversationInfo(c.getUserUUIDList()));
    }
}
