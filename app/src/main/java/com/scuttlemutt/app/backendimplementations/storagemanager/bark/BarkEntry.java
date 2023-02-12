package com.scuttlemutt.app.backendimplementations.storagemanager.bark;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

import types.Bark;
import types.Conversation;

/**
 * An object which represents a database entry for a Bark object.
 */
@Entity
public class BarkEntry {
    @PrimaryKey
    @NonNull
    public UUID uuid;

    @ColumnInfo(name = "barkJson")
    @NonNull
    public String barkJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, BarkEntry class is not a valid Entity.)
     */
    public BarkEntry(@NonNull final UUID uuid,
                     @NonNull final String barkJson) {
        this.uuid = uuid;
        this.barkJson = barkJson;
    }

    /**
     * Constructs a BarkEntry object from the passed Bark object.
     * @param bark The Bark object from which this entry is being constructed.
     */
    public BarkEntry(final Bark bark) {
        this.uuid = bark.getUniqueId();
        this.barkJson = new String(bark.toNetworkBytes());
    }

    public Bark toBark() {
        return Bark.fromNetworkBytes(barkJson.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BarkEntry)) {
            return false;
        }
        return this.uuid.equals(((BarkEntry) o).uuid);
    }
}
