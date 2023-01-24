package storagemanager;

import types.Bark;
import types.Conversation;
import types.MuttIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implements the StorageManager interface using a Map-based backend.
 *
 * NOTE:  We serialize/deserialize the object to copy it.  We want to copy/clone it so that we aren't modifying items
 * stored in memory.  Since the objects contain private fields, serialization allows us to easily clone them without
 * adding extra constructors/classes/methods/etc.
 */
public class MapStorageManager implements StorageManager {
    // maps
    private Map<UUID, String> barkMap;
    private Map<UUID, String> muttIdentifierMap;
    private Map<List<UUID>, String> conversationInfoMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<UUID, String>();
        this.muttIdentifierMap = new HashMap<UUID, String>();
        this.conversationInfoMap = new HashMap<List<UUID>, String>();
    }

    @Override
    public Bark lookupBark(final UUID barkUuid) {
        final String serializedObject = this.barkMap.getOrDefault(barkUuid, null);
        if (serializedObject == null) {
            return null;
        }
        return (Bark) SerializationHelper.deserializeStringToObject(serializedObject);
    }

    @Override
    public MuttIdentifier lookupMuttIdentifier(final UUID muttIdentifierUuid) {
        final String serializedObject = this.muttIdentifierMap.getOrDefault(muttIdentifierUuid, null);
        if (serializedObject == null) {
            return null;
        }
        return (MuttIdentifier) SerializationHelper.deserializeStringToObject(serializedObject);
    }

    @Override
    public Conversation lookupConversationInfo(final List<UUID> userUuidList) {
        final String serializedObject = this.conversationInfoMap.getOrDefault(userUuidList, null);
        if (serializedObject == null) {
            return null;
        }
        return (Conversation) SerializationHelper.deserializeStringToObject(serializedObject);
    }

    @Override
    public void storeBark(final Bark bark) {
        this.barkMap.put(bark.getUniqueId(), SerializationHelper.serializeObjectToString(bark));
    }

    @Override
    public void storeMuttIdentifier(final MuttIdentifier muttIdentifier) {
        this.muttIdentifierMap.put(muttIdentifier.getUniqueId(), SerializationHelper.serializeObjectToString(muttIdentifier));
    }

    @Override
    public void storeConversationInfo(final Conversation conversation) {
        this.conversationInfoMap.put(conversation.getUserUUIDList(), SerializationHelper.serializeObjectToString(conversation));
    }

    @Override
    public Bark deleteBark(final UUID barkUuid) {
        final String serializedObject = this.barkMap.remove(barkUuid);
        if (serializedObject == null) {
            return null;
        }
        return (Bark) SerializationHelper.deserializeStringToObject(serializedObject);
    }

    @Override
    public MuttIdentifier deleteMuttIdentifier(final UUID muttIdentifierUuid) {
        final String serializedObject = this.muttIdentifierMap.remove(muttIdentifierUuid);
        if (serializedObject == null) {
            return null;
        }
        return (MuttIdentifier) SerializationHelper.deserializeStringToObject(serializedObject);
    }

    @Override
    public Conversation deleteConversationInfo(final List<UUID> userUuidList) {
        final String serializedObject = this.conversationInfoMap.remove(userUuidList);
        if (serializedObject == null) {
            return null;
        }
        return (Conversation) SerializationHelper.deserializeStringToObject(serializedObject);
    }
}
