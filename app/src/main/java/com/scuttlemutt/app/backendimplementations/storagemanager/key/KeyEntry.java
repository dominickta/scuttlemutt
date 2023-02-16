package com.scuttlemutt.app.backendimplementations.storagemanager.key;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.crypto.SecretKey;

import types.DawgIdentifier;
import types.serialization.SerializationUtils;

/**
 * An object which represents a database entry for a Key object.
 */
@Entity
public class KeyEntry {
    @PrimaryKey
    @NonNull
    public String uuid;  // this should be the UUID of the associated DawgIdentifier.

    @ColumnInfo(name = "keyJson")
    @NonNull
    public String keyJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, KeyEntry class is not a valid Entity.)
     */
    public KeyEntry(@NonNull final String uuid,
                    @NonNull final String keyJson) {
        this.uuid = uuid;
        this.keyJson = keyJson;
    }

    /**
     * Constructs a KeyEntry object from the passed data.
     * @param dawgIdUuid The UUID of the DawgIdentifier object from which this entry is being constructed.
     * @param key The SecretKey object for which this entry is being constructed.
     */
    public KeyEntry(final UUID dawgIdUuid, final SecretKey key) {
        this.uuid = dawgIdUuid.toString();
        this.keyJson = new String(SerializationUtils.serializeKey(key));
    }

    public SecretKey getKey() {
        return SerializationUtils.deserializeSecretKey(this.keyJson.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyEntry)) {
            return false;
        }
        return this.uuid.equals(((KeyEntry) o).uuid);
    }
}
