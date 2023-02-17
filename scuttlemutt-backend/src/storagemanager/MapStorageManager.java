package storagemanager;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.serialization.SerializationUtils;

/**
 * Implements the StorageManager interface using a Map-based backend.
 *
 * NOTE: We serialize/deserialize the object to copy it. We want to copy/clone
 * it so that we aren't modifying items stored in memory. Since the objects
 * contain private fields, serialization allows us to easily clone them without
 * adding extra constructors/classes/methods/etc.
 */
public class MapStorageManager implements StorageManager {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // maps
    private final Map<UUID, String> barkMap;
    private final Map<String, PublicKey> deviceMap;
    private final Map<PublicKey, String> dawgIdentifierMap;
    private final Map<PublicKey, String> conversationMap;
    private final Map<PublicKey, String> keyMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<>();
        this.deviceMap = new HashMap<>();
        this.dawgIdentifierMap = new HashMap<>();
        this.conversationMap = new HashMap<>();
        this.keyMap = new HashMap<>();
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
    public DawgIdentifier lookupDawgIdentifier(final PublicKey theirPublicKey) {
        final String serializedObject = this.dawgIdentifierMap.getOrDefault(theirPublicKey, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, DawgIdentifier.class);
    }

    @Override
    public Conversation lookupConversation(final PublicKey theirPublicKey) {
        final String serializedObject = this.conversationMap.getOrDefault(theirPublicKey, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }

    @Override
    public SecretKey lookupSecretKeyForPublicKey(final PublicKey theirPublicKey) {
        final String serializedObject = this.keyMap.getOrDefault(theirPublicKey, null);
        if (serializedObject == null) {
            return null;
        }
        return SerializationUtils.deserializeSecretKey(serializedObject.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public PublicKey lookupPublicKeyForDeviceId(final String deviceId) {
        final PublicKey publicKey = this.deviceMap.getOrDefault(deviceId, null);
        if (publicKey == null) {
            return null;
        }
        return publicKey;
    }

    @Override
    public void storeBark(final Bark bark) {
        this.barkMap.put(bark.getUniqueId(), GSON.toJson(bark));
    }

    @Override
    public void storeDawgIdentifier(final DawgIdentifier dawgIdentifier) {
        this.dawgIdentifierMap.put(dawgIdentifier.getPublicKey(), GSON.toJson(dawgIdentifier));
    }

    @Override
    public void storeConversation(final Conversation conversation) {
        this.conversationMap.put(conversation.getOtherPerson().getPublicKey(), GSON.toJson(conversation));
    }

    @Override
    public void storeSecretKeyForPublicKey(PublicKey publicKey, SecretKey key) {
        this.keyMap.put(publicKey, new String(SerializationUtils.serializeKey(key)));
    }

    @Override
    public void storePublicKeyForDeviceId(final String deviceId, final PublicKey publicKey) {
        this.deviceMap.put(deviceId, publicKey);
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
    public DawgIdentifier deleteDawgIdentifier(final PublicKey publicKey) {
        final String serializedObject = this.dawgIdentifierMap.remove(publicKey);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, DawgIdentifier.class);
    }

    @Override
    public Conversation deleteConversation(final PublicKey publicKey) {
        final String serializedObject = this.conversationMap.remove(publicKey);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }

    @Override
    public SecretKey deleteSecretKeyForPublicKey(final PublicKey publicKey) {
        final String serializedObject = this.keyMap.remove(publicKey);
        if (serializedObject == null) {
            return null;
        }
        return SerializationUtils.deserializeSecretKey(serializedObject.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public PublicKey deletePublicKeyForDeviceId(final String deviceId) {
        final PublicKey publicKey = this.deviceMap.remove(deviceId);
        if (publicKey == null) {
            return null;
        }
        return publicKey;
    }

    @Override
    public List<DawgIdentifier> getAllDawgIdentifiers() {
        return this.dawgIdentifierMap.values()
                .stream()
                .map(s -> GSON.fromJson(s, DawgIdentifier.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Conversation> listAllConversations() {
        return this.conversationMap.values()
                .stream()
                .map(s -> GSON.fromJson(s, Conversation.class))
                .collect(Collectors.toList());
    }
}
