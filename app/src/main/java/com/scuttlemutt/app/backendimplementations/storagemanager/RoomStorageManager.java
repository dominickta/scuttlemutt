package com.scuttlemutt.app.backendimplementations.storagemanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierEntry;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

/**
 * StorageManager class which interfaces with Android's Room DB for a backend.
 */
public class RoomStorageManager implements StorageManager {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    private final AppDatabase appDb;

    /**
     * Constructs a RoomStorageManager object.
     * @param appDb  The AppDatabase object used for Room storage.  NOTE:  This object depends upon
     *               a Context object, so it _must_ be created at the activity level and passed in.
     */
    public RoomStorageManager(final AppDatabase appDb) {
        this.appDb = appDb;
    }

    @Override
    public Bark lookupBark(UUID barkUuid) {
        final BarkEntry be = this.appDb.barkDao().findByUuid(barkUuid.toString());
        return be != null ? be.toBark() : null;
    }

    @Override
    public DawgIdentifier lookupDawgIdentifier(UUID dawgIdentifierUuid) {
        final DawgIdentifierEntry de = this.appDb.dawgIdentifierDao().findByUuid(dawgIdentifierUuid.toString());
        return de != null ? de.toDawgIdentifier() : null;
    }

    @Override
    public Conversation lookupConversation(List<UUID> userUuidList) {
        // convert the userUuidList into a String so it can be feed to the Room DB.
        final String userUuidListString = GSON.toJson(userUuidList);

        // do the lookup.
        final ConversationEntry ce = this.appDb.conversationDao().findByUuidList(userUuidListString);
        return ce != null ? ce.toConversation() : null;
    }

    @Override
    public void storeBark(Bark bark) {
        this.appDb.barkDao().insertBarkEntry(new BarkEntry(bark));
    }

    @Override
    public void storeDawgIdentifier(DawgIdentifier dawgIdentifier) {
        this.appDb.dawgIdentifierDao().insertDawgIdentifierEntry(new DawgIdentifierEntry(dawgIdentifier));
    }

    @Override
    public void storeConversation(Conversation conversation) {
        this.appDb.conversationDao().insertConverationEntry(new ConversationEntry(conversation));
    }

    @Override
    public Bark deleteBark(UUID barkUuid) {
        // find the BarkEntry that needs to be deleted.
        final BarkEntry b = this.appDb.barkDao().findByUuid(barkUuid.toString());

        // delete the BarkEntry.
        this.appDb.barkDao().deleteBarkEntry(b);

        // return the Bark object associated with the deleted BarkEntry.
        return b.toBark();
    }

    @Override
    public DawgIdentifier deleteDawgIdentifier(UUID dawgIdentifierUuid) {
        // find the DawgIdentifierEntry that needs to be deleted.
        final DawgIdentifierEntry d = this.appDb.dawgIdentifierDao().findByUuid(dawgIdentifierUuid.toString());

        // delete the DawgIdentifierEntry.
        this.appDb.dawgIdentifierDao().deleteDawgIdentifierEntry(d);

        // return the DawgIdentifier object associated with the deleted DawgIdentifierEntry.
        return d.toDawgIdentifier();
    }

    @Override
    public Conversation deleteConversation(List<UUID> userUuidList) {
        // convert the userUuidList into a String so it can be feed to the Room DB.
        final String userUuidListString = GSON.toJson(userUuidList);

        // find the ConversationEntry that needs to be deleted.
        final ConversationEntry c = this.appDb.conversationDao().findByUuidList(userUuidListString);

        // delete the ConversationEntry.
        this.appDb.conversationDao().deleteConverationEntry(c);

        // return the Conversation object associated with the deleted ConversationEntry.
        return c.toConversation();
    }

    @Override
    public List<Conversation> listAllConversations() {
        final List<ConversationEntry> ces = this.appDb.conversationDao().listAllConversations();
        return ces.stream()
                .map(ConversationEntry::toConversation)
                .collect(Collectors.toList());
    }

    @Override
    public List<DawgIdentifier> getAllDawgIdentifiers() {
        final List<DawgIdentifierEntry> ces = this.appDb.dawgIdentifierDao().getAllDawgIdentifiers();
        return ces.stream()
                .map(DawgIdentifierEntry::toDawgIdentifier)
                .collect(Collectors.toList());
    }
}
