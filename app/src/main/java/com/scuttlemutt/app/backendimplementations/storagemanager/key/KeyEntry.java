package com.scuttlemutt.app.backendimplementations.storagemanager.key;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;
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

    @ColumnInfo(name = "keyListJson")
    @NonNull
    public String keyListJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, KeyEntry class is not a valid Entity.)
     */
    public KeyEntry(@NonNull final String uuid,
                    @NonNull final String keyListJson) {
        this.uuid = uuid;
        this.keyListJson = keyListJson;
    }

    /**
     * Constructs a KeyEntry object from the passed data.
     * @param dawgIdUuid The UUID of the DawgIdentifier object from which this entry is being constructed.
     * @param keyList A List<Key> containing the Keys for which this entry is being constructed.
     */
    public KeyEntry(final UUID dawgIdUuid, final List<Key> keyList) {
        this.uuid = dawgIdUuid.toString();
        this.keyListJson = SerializationUtils.serializeKeyList(keyList);
    }

    public List<Key> getKeys() {
        return SerializationUtils.deserializeKeyList(this.keyListJson);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyEntry)) {
            return false;
        }
        return this.uuid.equals(((KeyEntry) o).uuid);
    }
}
