package types;

import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Represents a unique identifier for users on the network.
 */
public class DawgIdentifier {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final String username;
    private final UUID dawgId;

    /**
     * Constructs a new DawgIdentifier.
     * 
     * @param username      The human-readable name for the user.
     * @param dawgId The user's UUID
     */
    public DawgIdentifier(final String username, final UUID dawgId) {
        this.username = username;
        this.dawgId = dawgId;
    }

    // public methods
    public String getUsername() {
        return this.username;
    }

    public UUID getUUID() {
        return this.dawgId;
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DawgIdentifier)) {
            return false;
        }
        return this.getUUID().equals(((DawgIdentifier) o).getUUID());
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
        return this.username + " (" + getUUID() + ")";
    }
}