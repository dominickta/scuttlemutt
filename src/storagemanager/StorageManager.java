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
    public Bark lookupBark(final UUID barkUuid);
    public MuttIdentifier lookupMuttIdentifier(final UUID muttIdentifierUuid);
    public Conversation lookupConversation(final List<UUID> userUuidList);

    // store*() methods
    //
    // if there are any preexisting objects, they are overwritten.
    public void storeBark(final Bark bark);
    public void storeMuttIdentifier(final MuttIdentifier muttIdentifier);
    public void storeConversation(final Conversation conversation);

    // delete*() methods
    //
    // returns the object which was deleted.  if no object was found, returns null.
    public Bark deleteBark(final UUID barkUuid);
    public MuttIdentifier deleteMuttIdentifier(final UUID muttIdentifierUuid);
    public Conversation deleteConversation(final List<UUID> userUuidList);
}