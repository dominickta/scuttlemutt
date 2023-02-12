package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import types.serialization.SerializationUtils;

import java.security.PublicKey;
import java.util.UUID;

/**
 * Represents a unique identifier for users on the network.
 */
public class DawgIdentifier {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final String userContact;
    private final UUID muttNetworkUUID;
    private byte[] publicKeyBytes;

    /**
     * Constructs a new DawgIdentifier.
     * @param userContact  The human-readable name for the user.
     * @param scuttlemuttNetworkUUID  A UUID that is directly associated with the user.  This will be provided during
     *                                initialization.
     * @param publicKey  The user's public key.  This is mutable since the user's key can change.
     */
    public DawgIdentifier(final String userContact, final UUID scuttlemuttNetworkUUID, final PublicKey publicKey) {
        this.userContact = userContact;
        this.muttNetworkUUID = scuttlemuttNetworkUUID;
        this.publicKeyBytes = SerializationUtils.serializeKey(publicKey);
    }

    // public methods
    public String getUserContact() {
        return this.userContact;
    }

    public UUID getUniqueId() {
        return this.muttNetworkUUID;
    }

    public PublicKey getPublicKey() {
        return SerializationUtils.deserializeRSAPublicKey(this.publicKeyBytes);
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKeyBytes = SerializationUtils.serializeKey(publicKey);
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DawgIdentifier)) {
            return false;
        }
        return this.getUniqueId().equals(((DawgIdentifier) o).getUniqueId());
    }

    /**
     * Returns a byte[] containing the bytes which represent the DawgIdentifier.
     *
     * @return a byte[] containing the bytes which represent the DawgIdentifier.
     */
    public byte[] toNetworkBytes() {
        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a DawgIdentifier derived from the passed byte[].
     *
     * @return a DawgIdentifier derived from the passed byte[].
     */
    public static DawgIdentifier fromNetworkBytes(final byte[] dawgIdentifierBytes) {
        // TODO:  Add decryption here.
        return GSON.fromJson(new String(dawgIdentifierBytes), DawgIdentifier.class);
    }

    @Override
    public String toString() {
        return "userContact:  " + this.userContact + "\tmuttNetworkUUID:  " + this.muttNetworkUUID.toString() + "\tpublicKey:  " + new String(this.publicKeyBytes);
    }
}