package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

/**
 * Represents a unique identifier for users on the network.
 */
public class DawgIdentifier {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final String userContact;
    private final UUID muttNetworkUUID;

    /**
     * Constructs a new DawgIdentifier.
     * @param userContact  The human-readable name for the user.
     * @param scuttlemuttNetworkUUID  A UUID that is directly associated with the user.  This will be provided during
     *                                initialization.
     */
    public DawgIdentifier(final String userContact, final UUID scuttlemuttNetworkUUID) {
        this.userContact = userContact;
        this.muttNetworkUUID = scuttlemuttNetworkUUID;
    }

    // public methods
    public String getUserContact() {
        return this.userContact;
    }

    public UUID getUniqueId() {
        return this.muttNetworkUUID;
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
        return GSON.fromJson(new String(dawgIdentifierBytes), DawgIdentifier.class);
    }

    @Override
    public String toString() {
        return "userContact:  " + this.userContact + "\tmuttNetworkUUID:  " + this.muttNetworkUUID.toString();
    }
}