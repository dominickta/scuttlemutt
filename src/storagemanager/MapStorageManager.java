package storagemanager;

import com.google.gson.Gson;
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
    private static final Gson GSON = new Gson();

    // maps
    private final Map<UUID, String> barkMap;
    private final Map<UUID, String> muttIdentifierMap;
    private final Map<List<UUID>, String> conversationMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<UUID, String>();
        this.muttIdentifierMap = new HashMap<UUID, String>();
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
    public MuttIdentifier lookupMuttIdentifier(final UUID muttIdentifierUuid) {
        final String serializedObject = this.muttIdentifierMap.getOrDefault(muttIdentifierUuid, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, MuttIdentifier.class);
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
    public void storeMuttIdentifier(final MuttIdentifier muttIdentifier) {
        this.muttIdentifierMap.put(muttIdentifier.getUniqueId(), GSON.toJson(muttIdentifier));
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
    public MuttIdentifier deleteMuttIdentifier(final UUID muttIdentifierUuid) {
        final String serializedObject = this.muttIdentifierMap.remove(muttIdentifierUuid);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, MuttIdentifier.class);
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
