package types;

import java.security.PublicKey;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import types.serialization.SerializationUtils;

/**
 * Represents a unique identifier for users on the network.
 */
public class DawgIdentifier {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final String username;
    private final byte[] userPublicKey;

    /**
     * Constructs a new DawgIdentifier.
     * 
     * @param username      The human-readable name for the user.
     * @param userPublicKey The user's public key, provided during
     *                      initialization.
     */
    public DawgIdentifier(final String username, final PublicKey userPublicKey) {
        this.username = username;
        this.userPublicKey = SerializationUtils.serializeKey(userPublicKey);
    }

    // public methods
    public String getUsername() {
        return this.username;
    }

    public PublicKey getPublicKey() {
        return SerializationUtils.deserializePublicKey(this.userPublicKey);
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DawgIdentifier)) {
            return false;
        }
        return this.getPublicKey().equals(((DawgIdentifier) o).getPublicKey());
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
        String pubKey = Base64.getEncoder().encodeToString(getPublicKey().getEncoded());
        return this.username + " (0x" + pubKey + ")";
    }
}