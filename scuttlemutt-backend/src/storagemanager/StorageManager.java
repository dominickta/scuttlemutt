package storagemanager;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    // the maximum number of keys which should be stored for a particular DawgIdentifier.
    // once we store more than this value, we throw out the oldest stored Key for the ID from the DB.
    public static final int MAX_NUM_HISTORICAL_KEYS_TO_STORE = 5;

    // lookup*() methods
    //
    // returns "null" if not found.
    Bark lookupBark(final UUID barkUuid);
    DawgIdentifier lookupDawgIdentifier(final UUID id);
    Conversation lookupConversation(final UUID id);
    PublicKey lookupPublicKeyForDeviceId(final String deviceId);
    PublicKey lookupPublicKeyForUUID(final UUID id);
    List<SecretKey> lookupSecretKeysForUUID(final UUID id);
    Message lookupMessage(final UUID id);
    PrivateKey lookupPrivateKey();
    
    // convenience method for looking-up latest Key for user.
    default SecretKey lookupLatestKeyForDawgIdentifier(final UUID id) {
        return this.lookupSecretKeysForUUID(id).get(0);
    }

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);
    void storeConversation(final Conversation conversation);
    void storePublicKeyForDeviceId(final String deviceId, final PublicKey publicKey);
    void storeSecretKeyForUUID(final UUID id, final SecretKey key);
    void storePublicKeyForUUID(final UUID id, final PublicKey key);
    void storePrivateKey(final PrivateKey privateKey);
    void storeMessage(final Message message);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    DawgIdentifier deleteDawgIdentifier(final UUID id);
    Conversation deleteConversation(final UUID id);
    PublicKey deletePublicKeyForDeviceId(final String deviceId);
    PublicKey deletePublicKeyForUUID(final UUID id);
    List<SecretKey> deleteKeysForUUID(final UUID id);
    PrivateKey deletePrivateKey();
    Message deleteMessage(final UUID id);

    // list*() methods
    List<Conversation> listAllConversations();
    List<DawgIdentifier> getAllDawgIdentifiers();
}