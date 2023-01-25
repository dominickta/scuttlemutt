package storagemanager;

import types.Bark;
import types.Conversation;
import types.MuttIdentifier;

import java.util.List;
import java.util.UUID;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    // lookup*() methods
    //
    // returns "null" if not found.
    Bark lookupBark(final UUID barkUuid);
    MuttIdentifier lookupMuttIdentifier(final UUID muttIdentifierUuid);
    Conversation lookupConversation(final List<UUID> userUuidList);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    void storeBark(final Bark bark);
    void storeMuttIdentifier(final MuttIdentifier muttIdentifier);
    void storeConversation(final Conversation conversation);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    Bark deleteBark(final UUID barkUuid);
    MuttIdentifier deleteMuttIdentifier(final UUID muttIdentifierUuid);
    Conversation deleteConversation(final List<UUID> userUuidList);
}