package storagemanager;

import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

import java.security.Key;
import java.util.List;
import java.util.UUID;

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
    DawgIdentifier lookupDawgIdentifier(final UUID dawgIdentifierUuid);
    Conversation lookupConversation(final List<UUID> userUuidList);
    List<Key> lookupKeysForDawgIdentifier(final UUID dawgIdentifierUuid);
    default Key lookupLatestKeyForDawgIdentifier(final UUID dawgIdentifierUuid) {  // convenience method for looking-up latest Key for user.
        List<Key> keys = this.lookupKeysForDawgIdentifier(dawgIdentifierUuid);
        return keys.get(keys.size()-1);
    }
    Message lookupMessage(final UUID messageUuid);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeDawgIdentifier(final DawgIdentifier dawgIdentifier);
    void storeConversation(final Conversation conversation);
    void storeKeyForDawgIdentifier(final UUID dawgIdentifierUuid, final Key key);
    void storeMessage(final Message message);


    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    DawgIdentifier deleteDawgIdentifier(final UUID dawgIdentifierUuid);
    Conversation deleteConversation(final List<UUID> userUuidList);
    List<Key> deleteKeysForDawgIdentifier(final UUID dawgIdentifierUuid);
    Message deleteMessage(final UUID messageUuid);

    // list*() methods
    List<Conversation> listAllConversations();

    List<DawgIdentifier> getAllDawgIdentifiers();
}