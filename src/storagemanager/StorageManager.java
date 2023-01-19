package storagemanager;

import types.Bark;
import types.ConversationInfo;
import types.MuttIdentifier;

import java.util.List;
import java.util.UUID;

/**
 * This interface specifies how the StorageManager objects should be written.
 */
public interface StorageManager {
    // lookup*() methods
    public Bark lookupBark(final UUID barkId);
    public MuttIdentifier lookupMuttIdentifier(final UUID muttIdentifierUuid);
    public ConversationInfo lookupConversationInfo(final List<UUID> userUuidList);

    // store*() methods
    public void storeBark(final Bark bark);
    public void storeMuttIdentifier(final MuttIdentifier muttIdentifier);
    public void storeConversationInfo(final ConversationInfo conversationInfo);

    // delete*() methods
    public Bark deleteBark(final UUID barkId);
    public MuttIdentifier deleteMuttIdentifier(final UUID muttIdentifierUuid);
    public ConversationInfo deleteConversationInfo(final List<UUID> userUuidList);
}