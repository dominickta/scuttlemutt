package com.scuttlemutt.app.backendimplementations.storagemanager.dawgidentifier;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.scuttlemutt.app.backendimplementations.storagemanager.bark.BarkEntry;

import java.util.UUID;

import types.Conversation;
import types.DawgIdentifier;

/**
 * An object which represents a database entry for a DawgIdentifier object.
 */
@Entity
public class DawgIdentifierEntry {
    @PrimaryKey
    @NonNull
    public String uuid;

    @NonNull
    public String username;

    @ColumnInfo(name = "dawgIdentifierJson")
    @NonNull
    public String dawgIdentifierJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, DawgIdentifierEntry class is not a valid Entity.)
     */
    public DawgIdentifierEntry(@NonNull final String uuid,
                               @NonNull final String username,
                               @NonNull final String dawgIdentifierJson) {
        this.uuid = uuid;
        this.username = username;
        this.dawgIdentifierJson = dawgIdentifierJson;
    }

    /**
     * Constructs a DawgIdentifierEntry object from the passed DawgIdentifier object.
     * @param dawgId The DawgIdentifier object from which this entry is being constructed.
     */
    public DawgIdentifierEntry(final DawgIdentifier dawgId) {
        this.uuid = dawgId.getUUID().toString();
        this.username = dawgId.getUsername();
        this.dawgIdentifierJson = new String(dawgId.toNetworkBytes());
    }

    public DawgIdentifier toDawgIdentifier() {
        return DawgIdentifier.fromNetworkBytes(dawgIdentifierJson.getBytes());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DawgIdentifierEntry)) {
            return false;
        }
        return this.uuid.equals(((DawgIdentifierEntry) o).uuid);
    }
}
