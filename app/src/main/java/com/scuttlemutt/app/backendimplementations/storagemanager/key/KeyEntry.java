package com.scuttlemutt.app.backendimplementations.storagemanager.key;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
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

    @ColumnInfo(name = "symmetricKeyListJson")
    @NonNull
    public String symmetricKeyListJson;

    @ColumnInfo(name = "publicKeyJson")
    @NonNull
    public String publicKeyJson;

    @ColumnInfo(name = "privateKeyJson")
    @NonNull
    public String privateKeyJson;

    /**
     * Required POJO field-based constructor.  This is used by Room when retrieving + returning DB data.
     *
     * (Otherwise, KeyEntry class is not a valid Entity.)
     */
    public KeyEntry(@NonNull final String uuid,
                    @NonNull final String symmetricKeyListJson,
                    @NonNull final String publicKeyJson,
                    @NonNull final String privateKeyJson
    ) {
        this.uuid = uuid;
        this.symmetricKeyListJson = symmetricKeyListJson;
        this.publicKeyJson = publicKeyJson;
        this.privateKeyJson = privateKeyJson;
    }

    /**
     * Constructs a KeyEntry object from the passed data.
     * @param dawgIdUuid The UUID of the DawgIdentifier object from which this entry is being constructed.
     * @param keyList A List<Key> containing the Keys for which this entry is being constructed.
     */
    public KeyEntry(final UUID dawgIdUuid, final List<Key> keyList) {
        this.uuid = dawgIdUuid.toString();
        if (keyList.size() > 0) {
            if (keyList.get(0) instanceof SecretKey) {
                this.symmetricKeyListJson = SerializationUtils.serializeKeyList(keyList);
                this.publicKeyJson = "";
                this.privateKeyJson = "";
            } else if (keyList.get(0) instanceof PublicKey) {
                this.symmetricKeyListJson = "";
                this.publicKeyJson = SerializationUtils.serializeKeyList(keyList);
                this.privateKeyJson = "";
            } else if (keyList.get(0) instanceof PrivateKey) {
                this.symmetricKeyListJson = "";
                this.publicKeyJson = "";
                this.privateKeyJson = SerializationUtils.serializeKeyList(keyList);
            }
        } else {
            throw new RuntimeException("key list was empty");
        }
    }

    public List<SecretKey> getSymmetricKeys() {
        // since we're casting downwards, we have to cast through a wildcard
        return (List<SecretKey>)(List<?>) SerializationUtils.deserializeKeyList(this.symmetricKeyListJson);
    }

    public List<PublicKey> getPublicKeys() {
        // since we're casting downwards, we have to cast through a wildcard
        return (List<PublicKey>)(List<?>) SerializationUtils.deserializeKeyList(this.publicKeyJson);
    }

    public List<PrivateKey> getPrivateKeys() {
        // since we're casting downwards, we have to cast through a wildcard
        return (List<PrivateKey>)(List<?>) SerializationUtils.deserializeKeyList(this.privateKeyJson);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyEntry)) {
            return false;
        }
        return this.uuid.equals(((KeyEntry) o).uuid);
    }
}
