package storagemanager;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    // lookup*() methods
    //
    // returns "null" if not found.
    Bark lookupBark(final UUID barkUuid);
    DawgIdentifier lookupDawgIdentifier(final UUID id);
    Conversation lookupConversation(final UUID id);
    PublicKey lookupPublicKeyForDeviceId(final String deviceId);
    SecretKey lookupSecretKeyForUUID(final UUID id);
    PublicKey lookupPublicKeyForUUID(final UUID id);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);
    void storeConversation(final Conversation conversation);
    void storePublicKeyForDeviceId(final String deviceId, final PublicKey publicKey);
    void storeSecretKeyForUUID(final UUID id, final SecretKey key);
    void storePublicKeyForUUID(final UUID id, final PublicKey key);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    DawgIdentifier deleteDawgIdentifier(final UUID id);
    Conversation deleteConversation(final UUID id);
    PublicKey deletePublicKeyForDeviceId(final String deviceId);
    SecretKey deleteSecretKeyForUUID(final UUID id);
    PublicKey deletePublicKeyForUUID(final UUID id);

    // list*() methods
    List<Conversation> listAllConversations();
    List<DawgIdentifier> getAllDawgIdentifiers();
}