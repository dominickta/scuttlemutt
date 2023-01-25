package storagemanager;

import com.google.gson.Gson;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

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
    private static final Gson GSON = new Gson();

    // maps
    private final Map<UUID, String> barkMap;
    private final Map<UUID, String> dawgIdentifierMap;
    private final Map<List<UUID>, String> conversationMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<UUID, String>();
        this.dawgIdentifierMap = new HashMap<UUID, String>();
        this.conversationMap = new HashMap<List<UUID>, String>();
    }

    @Override
    public Bark lookupBark(final UUID barkUuid) {
        final String serializedObject = this.barkMap.getOrDefault(barkUuid, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Bark.class);
    }

    @Override
    public DawgIdentifier lookupDawgIdentifier(final UUID dawgIdentifierUuid) {
        final String serializedObject = this.dawgIdentifierMap.getOrDefault(dawgIdentifierUuid, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, DawgIdentifier.class);
    }

    @Override
    public Conversation lookupConversation(final List<UUID> userUuidList) {
        final String serializedObject = this.conversationMap.getOrDefault(userUuidList, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }

    @Override
    public void storeBark(final Bark bark) {
        this.barkMap.put(bark.getUniqueId(), GSON.toJson(bark));
    }

    @Override
    public void storeDawgIdentifier(final DawgIdentifier dawgIdentifier) {
        this.dawgIdentifierMap.put(dawgIdentifier.getUniqueId(), GSON.toJson(dawgIdentifier));
    }

    @Override
    public void storeConversation(final Conversation conversation) {
        this.conversationMap.put(conversation.getUserUUIDList(), GSON.toJson(conversation));
    }

    @Override
    public Bark deleteBark(final UUID barkUuid) {
        final String serializedObject = this.barkMap.remove(barkUuid);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Bark.class);
    }

    @Override
    public DawgIdentifier deleteDawgIdentifier(final UUID dawgIdentifierUuid) {
        final String serializedObject = this.dawgIdentifierMap.remove(dawgIdentifierUuid);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, DawgIdentifier.class);
    }

    @Override
    public Conversation deleteConversation(final List<UUID> userUuidList) {
        final String serializedObject = this.conversationMap.remove(userUuidList);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }
}
