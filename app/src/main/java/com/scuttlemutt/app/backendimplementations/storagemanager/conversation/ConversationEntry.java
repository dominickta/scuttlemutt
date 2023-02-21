package com.scuttlemutt.app.backendimplementations.storagemanager.conversation;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import types.Conversation;

/**
 * An object which represents a database entry for a Conversation object.
 */
@Entity
public class ConversationEntry {
    @PrimaryKey
    @NonNull
    public String userIdJson;

    @ColumnInfo(name = "conversationJson")
    @NonNull
    public String conversationJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, ConversationEntry class is not a valid Entity.)
     */
    public ConversationEntry(@NonNull final String userIdJson,
                             @NonNull final String conversationJson) {
        this.userIdJson = userIdJson;
        this.conversationJson = conversationJson;
    }

    /**
     * Constructs a ConversationEntry object from the passed Conversation object.
     * @param c The Conversation object from which this entry is being constructed.
     */
    public ConversationEntry(final Conversation c) {
        // JSON-ify the User UUID List we're using as an entry key.
        final Gson GSON = new GsonBuilder().setLenient().create();
        this.userIdJson = GSON.toJson(c.getOtherPerson().getUUID());

        // store the JSON for the entire Conversation.
        this.conversationJson = new String(c.toNetworkBytes());
    }

    public Conversation toConversation() {
        return Conversation.fromNetworkBytes(conversationJson.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConversationEntry)) {
            return false;
        }
        return this.userIdJson.equals(((ConversationEntry) o).userIdJson);
    }
}
