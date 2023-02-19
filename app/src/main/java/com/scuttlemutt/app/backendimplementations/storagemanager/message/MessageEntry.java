package com.scuttlemutt.app.backendimplementations.storagemanager.message;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import types.Message;

/**
 * An object which represents a database entry for a Message object.
 */
@Entity
public class MessageEntry {
    @PrimaryKey
    @NonNull
    public String uuid;

    @ColumnInfo(name = "messageJson")
    @NonNull
    public String messageJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, MessageEntry class is not a valid Entity.)
     */
    public MessageEntry(@NonNull final String uuid,
                        @NonNull final String messageJson) {
        this.uuid = uuid;
        this.messageJson = messageJson;
    }

    /**
     * Constructs a MessageEntry object from the passed Message object.
     * @param m The Message object from which this entry is being constructed.
     */
    public MessageEntry(final Message m) {
        this.uuid = m.getUniqueId().toString();
        this.messageJson = new String(m.toNetworkBytes());
    }

    public Message toMessage() {
        return Message.fromNetworkBytes(messageJson.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageEntry)) {
            return false;
        }
        return this.uuid.equals(((MessageEntry) o).uuid);
    }
}
