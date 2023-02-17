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
    DawgIdentifier lookupDawgIdentifier(final PublicKey theirPublicKey);
    Conversation lookupConversation(final PublicKey theirPublicKey);
    SecretKey lookupSecretKeyForPublicKey(final PublicKey publicKey);
    PublicKey lookupPublicKeyForDeviceId(final String deviceId);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);
    void storeConversation(final Conversation conversation);
    void storeSecretKeyForPublicKey(final PublicKey publicKey, final SecretKey key);
    void storePublicKeyForDeviceId(final String deviceId, final PublicKey publicKey);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    DawgIdentifier deleteDawgIdentifier(final PublicKey publicKey);
    Conversation deleteConversation(final PublicKey publicKey);
    SecretKey deleteSecretKeyForPublicKey(final PublicKey publicKey);
    PublicKey deletePublicKeyForDeviceId(final String deviceId);

    // list*() methods
    List<Conversation> listAllConversations();

    List<DawgIdentifier> getAllDawgIdentifiers();
}