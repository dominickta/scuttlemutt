package storagemanager;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    // lookup*() methods
    //
    // returns "null" if not found.
    Bark lookupBark(final UUID barkUuid);
    DawgIdentifier lookupDawgIdentifier(final UUID dawgIdentifierUuid);
    Conversation lookupConversation(final List<UUID> userUuidList);
    SecretKey lookupKeyForDawgIdentifier(final UUID dawgIdentifierUuid);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);
    void storeConversation(final Conversation conversation);
    void storeKeyForDawgIdentifier(final UUID dawgIdentifierUuid, final SecretKey key);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    DawgIdentifier deleteDawgIdentifier(final UUID dawgIdentifierUuid);
    Conversation deleteConversation(final List<UUID> userUuidList);
    SecretKey deleteKeyForDawgIdentifier(final UUID dawgIdentifierUuid);

    // list*() methods
    List<Conversation> listAllConversations();

    List<DawgIdentifier> getAllDawgIdentifiers();
}