package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

/**
 * Simple object type used to store plaintext messages w/ UUIDs.
 *
 * This class should be used for...
 * - Storing plaintext messages on-device so we don't have to depend on Barks + decryption to read
 *   previously-received messages.
 * - Linking plaintext messages to Conversations via UUIDs.
 */
public class Message {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final UUID uniqueId;
    private final String message;
    private final Long orderNum;

    /**
     * Constructs the Message object.
     * @param plaintextMessage  The plaintext message being stored in the Message object.
     * @param orderNum  The number of the Message used for ordering the messages in the UI.
     */
    public Message(final String plaintextMessage, final Long orderNum) {
        this.uniqueId = UUID.randomUUID();
        this.message = plaintextMessage;
        this.orderNum = orderNum;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getPlaintextMessage() {
        return this.message;
    }

    public Long getOrderNum() {
        return this.orderNum;
    }

    /**
     * Returns a byte[] containing the bytes which represent the Message.
     *
     * @return a byte[] containing the bytes which represent the Message.
     */
    public byte[] toNetworkBytes() {
        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a Message derived from the passed byte[].
     *
     * @return a Message derived from the passed byte[].
     */
    public static Message fromNetworkBytes(final byte[] messageBytes) {
        return GSON.fromJson(new String(messageBytes), Message.class);
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Message)) {
            return false;
        }
        return this.getPlaintextMessage().equals(((Message) o).getPlaintextMessage())
                && this.getOrderNum().equals(((Message) o).getOrderNum());
    }

    @Override
    public int hashCode() {
        return this.getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        return "plaintextMessage:  " + this.getPlaintextMessage() + "\tuniqueId:  " + this.uniqueId.toString();
    }
}
