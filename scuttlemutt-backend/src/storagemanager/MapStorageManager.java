package storagemanager;

import java.security.PrivateKey;
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
import types.Message;

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
    private PrivateKey privateKey;
    private final Map<UUID, String> barkMap;
    private final Map<UUID, Message> messageMap;
    private final Map<String, PublicKey> deviceMap;
    private final Map<UUID, PublicKey> publicKeyMap;
    private final Map<UUID, String> conversationMap;
    private final Map<UUID, List<SecretKey>> secretKeysMap;
    private final Map<UUID, DawgIdentifier> dawgIdentifierMap;

    public MapStorageManager() {
        this.barkMap = new HashMap<>();
        this.messageMap = new HashMap<>();
        this.deviceMap = new HashMap<>();
        this.dawgIdentifierMap = new HashMap<>();
        this.conversationMap = new HashMap<>();
        this.secretKeysMap = new HashMap<>();
        this.publicKeyMap = new HashMap<>();
        this.privateKey = null;
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
    public List<SecretKey> lookupSecretKeysForUUID(final UUID id) {
        return this.secretKeysMap.getOrDefault(id, null);
    }

    @Override
    public PublicKey lookupPublicKeyForUUID(final UUID id) {
        return this.publicKeyMap.getOrDefault(id, null);
    }

    @Override
    public PrivateKey lookupPrivateKey() {
        return this.privateKey;
    }

    @Override
    public Message lookupMessage(UUID id) {
        return this.messageMap.getOrDefault(id, null);
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
        // see if the obtained keyList is at the maximum size. if it is, remove the
        // oldest entry at index == 0.
        List<SecretKey> keyList = this.secretKeysMap.get(id);
        if (keyList.size() == StorageManager.MAX_NUM_HISTORICAL_KEYS_TO_STORE) {
            keyList.remove(0);
        }
        keyList.add(key);
    }

    @Override
    public void storePublicKeyForUUID(final UUID id, final PublicKey key) {
        this.publicKeyMap.put(id, key);
    }

    @Override
    public void storePrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public void storeMessage(final Message message) {
        this.messageMap.put(message.getUniqueId(), message);
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
    public List<SecretKey> deleteKeysForUUID(final UUID id) {
        return this.secretKeysMap.remove(id);
    }

    @Override
    public PublicKey deletePublicKeyForUUID(final UUID id) {
        return this.publicKeyMap.remove(id);
    }

    @Override
    public PrivateKey deletePrivateKey() {
        PrivateKey privateKey = this.privateKey;
        this.privateKey = null;
        return privateKey;
    }

    @Override
    public Message deleteMessage(UUID messageUuid) {
        return this.messageMap.remove(messageUuid);
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
