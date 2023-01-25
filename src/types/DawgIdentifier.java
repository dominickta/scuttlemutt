package types;

import java.util.UUID;

/**
 * Represents a unique identifier for users on the network.
 */
public class DawgIdentifier {
    // class variables
    private final String userContact;
    private final UUID muttNetworkUUID;
    private String publicKey;

    /**
     * Constructs a new DawgIdentifier.
     * @param userContact  The human-readable name for the user.
     * @param scuttlemuttNetworkUUID  A UUID that is directly associated with the user.  This will be provided during
     *                                initialization.
     * @param publicKey  The user's public key.  This is mutable since the user's key can change.
     */
    public DawgIdentifier(final String userContact, final UUID scuttlemuttNetworkUUID, final String publicKey) {
        this.userContact = userContact;
        this.muttNetworkUUID = scuttlemuttNetworkUUID;
        this.publicKey = publicKey;
    }

    // public methods
    public String getUserContact() {
        return this.userContact;
    }

    public UUID getUniqueId() {
        return this.muttNetworkUUID;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(final String newPublicKey) {
        this.publicKey = newPublicKey;
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DawgIdentifier)) {
            return false;
        }
        return this.getUniqueId().equals(((DawgIdentifier) o).getUniqueId());
    }

    @Override
    public String toString() {
        return "userContact:  " + this.userContact + "\tmuttNetworkUUID:  " + this.muttNetworkUUID.toString() + "\tpublicKey:  " + this.publicKey;
    }
}