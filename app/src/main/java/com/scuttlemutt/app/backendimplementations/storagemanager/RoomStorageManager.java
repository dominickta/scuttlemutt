package com.scuttlemutt.app.backendimplementations.storagemanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.conversation.ConversationEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier.DawgIdentifierEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.key.KeyEntry;
import com.scuttlemutt.app.backendimplementations.storagemanager.message.MessageEntry;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

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
    public DawgIdentifier lookupDawgIdentifierForUuid(UUID dawgIdentifierUuid) {
        final DawgIdentifierEntry de = this.appDb.dawgIdentifierDao().findByUuid(dawgIdentifierUuid.toString());
        return de != null ? de.toDawgIdentifier() : null;
    }

    @Override
    public DawgIdentifier lookupDawgIdentifierForUserContact(String userContact) {
        final DawgIdentifierEntry de = this.appDb.dawgIdentifierDao().findByUserContact(userContact);
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
    public List<Key> lookupKeysForDawgIdentifier(UUID dawgIdentifierUuid) {
        // lookup the KeyEntry.
        final KeyEntry ke = this.appDb.keyDao().findByUuid(dawgIdentifierUuid.toString());

        // return the key if it was found.  otherwise, return null.
        return ke != null ? ke.getKeys() : null;
    }

    @Override
    public Message lookupMessage(UUID messageUuid) {
        // lookup the MessageEntry.
        final MessageEntry me = this.appDb.messageDao().findByUuid(messageUuid.toString());

        // return the Message if a MessageEntry was found.  otherwise, return null.
        return me != null ? me.toMessage() : null;
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
    public void storeKeyForDawgIdentifier(UUID dawgIdentifierUuid, Key key) {
        // lookup to see if we're already storing a List of Keys for this UUID.  if we are storing
        // a List of Keys, we'll want to store an updated List of the Keys.
        final List<Key> keyList;
        final KeyEntry existingKeyEntry = this.appDb.keyDao().findByUuid(dawgIdentifierUuid.toString());
        if (existingKeyEntry != null) {
            keyList = existingKeyEntry.getKeys();
        } else {
            keyList = new ArrayList<Key>();
        }

        // see if the obtained keyList is at the maximum size.  if it is, remove the oldest entry
        // at index == 0.
        if (keyList.size() == StorageManager.MAX_NUM_HISTORICAL_KEYS_TO_STORE) {
            keyList.remove(0);
        }

        // append the new Key to the end of the List.
        keyList.add(key);

        // construct a KeyEntry containing the new Key + store it.
        final KeyEntry newKeyEntry = new KeyEntry(dawgIdentifierUuid, keyList);
        this.appDb.keyDao().insertKeyEntry(newKeyEntry);
    }

    @Override
    public void storeMessage(Message message) {
        this.appDb.messageDao().insertMessageEntry(new MessageEntry(message));
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
    public DawgIdentifier deleteDawgIdentifierByUuid(UUID dawgIdentifierUuid) {
        // find the DawgIdentifierEntry that needs to be deleted.
        final DawgIdentifierEntry d = this.appDb.dawgIdentifierDao().findByUuid(dawgIdentifierUuid.toString());

        // delete the DawgIdentifierEntry.
        this.appDb.dawgIdentifierDao().deleteDawgIdentifierEntry(d);

        // return the DawgIdentifier object associated with the deleted DawgIdentifierEntry.
        return d.toDawgIdentifier();
    }

    @Override
    public DawgIdentifier deleteDawgIdentifierByUserContact(String userContact) {
        // find the DawgIdentifierEntry that needs to be deleted.
        final DawgIdentifierEntry d = this.appDb.dawgIdentifierDao().findByUserContact(userContact);

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
    public List<Key> deleteKeysForDawgIdentifier(UUID dawgIdentifierUuid) {
        // lookup the KeyEntry.
        final KeyEntry ke = this.appDb.keyDao().findByUuid(dawgIdentifierUuid.toString());

        // delete the KeyEntry.
        this.appDb.keyDao().deleteKeyEntry(ke);

        // return the Key object associated with the deleted KeyEntry.
        return ke.getKeys();
    }

    @Override
    public Message deleteMessage(UUID messageUuid) {
        // find the MessageEntry that needs to be deleted.
        final MessageEntry me = this.appDb.messageDao().findByUuid(messageUuid.toString());

        // delete the MessageEntry.
        this.appDb.messageDao().deleteMessageEntry(me);

        // return the Message object associated with the deleted MessageEntry.
        return me.toMessage();
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
