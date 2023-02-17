package storagemanager;

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
    private final Map<UUID, DawgIdentifier> dawgIdentifierMap;
    private final Map<UUID, String> conversationMap;
    private final Map<UUID, SecretKey> secretKeyMap;
    private final Map<UUID, PublicKey> publicKeyMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<>();
        this.deviceMap = new HashMap<>();
        this.dawgIdentifierMap = new HashMap<>();
        this.conversationMap = new HashMap<>();
        this.secretKeyMap = new HashMap<>();
        this.publicKeyMap = new HashMap<>();
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
    public DawgIdentifier lookupDawgIdentifier(final UUID id) {
        return this.dawgIdentifierMap.getOrDefault(id, null);
    }

    @Override
    public Conversation lookupConversation(final UUID id) {
        final String serializedObject = this.conversationMap.getOrDefault(id, null);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }

    @Override
    public PublicKey lookupPublicKeyForDeviceId(final String deviceId) {
        return this.deviceMap.getOrDefault(deviceId, null);
    }

    @Override
    public SecretKey lookupSecretKeyForUUID(final UUID id) {
        return this.secretKeyMap.getOrDefault(id, null);
    }

    @Override
    public PublicKey lookupPublicKeyForUUID(final UUID id) {
        return this.publicKeyMap.getOrDefault(id, null);
    }

    @Override
    public void storeBark(final Bark bark) {
        this.barkMap.put(bark.getUniqueId(), GSON.toJson(bark));
    }

    @Override
    public void storeDawgIdentifier(final DawgIdentifier dawgIdentifier) {
        this.dawgIdentifierMap.put(dawgIdentifier.getUUID(), dawgIdentifier);
    }

    @Override
    public void storeConversation(final Conversation conversation) {
        this.conversationMap.put(conversation.getOtherPerson().getUUID(), GSON.toJson(conversation));
    }

    @Override
    public void storePublicKeyForDeviceId(final String deviceId, final PublicKey publicKey) {
        this.deviceMap.put(deviceId, publicKey);
    }

    @Override
    public void storeSecretKeyForUUID(final UUID id, final SecretKey key) {
        this.secretKeyMap.put(id, key);
    }

    @Override
    public void storePublicKeyForUUID(final UUID id, final PublicKey key) {
        this.publicKeyMap.put(id, key);
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
    public DawgIdentifier deleteDawgIdentifier(final UUID id) {
        return this.dawgIdentifierMap.remove(id);
    }

    @Override
    public Conversation deleteConversation(final UUID id) {
        final String serializedObject = this.conversationMap.remove(id);
        if (serializedObject == null) {
            return null;
        }
        return GSON.fromJson(serializedObject, Conversation.class);
    }

    @Override
    public PublicKey deletePublicKeyForDeviceId(final String deviceId) {
        final PublicKey publicKey = this.deviceMap.remove(deviceId);
        return publicKey;
    }

    @Override
    public SecretKey deleteSecretKeyForUUID(final UUID id) {
        return this.secretKeyMap.remove(id);
    }

    @Override
    public PublicKey deletePublicKeyForUUID(final UUID id) {
        return this.publicKeyMap.remove(id);
    }

    @Override
    public List<DawgIdentifier> getAllDawgIdentifiers() {
        return this.dawgIdentifierMap.values()
                .stream()
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
