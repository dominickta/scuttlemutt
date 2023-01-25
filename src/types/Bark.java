package types;

import storagemanager.SerializationHelper;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a "bark" (message) sent by the user.
 */
public class Bark implements Serializable {
    // constants

    // stores the maximum number of characters allowed in a Bark.
    public static final int UUID_SIZE = UUID.randomUUID().toString().getBytes().length;
    public static final int MAX_MESSAGE_SIZE = 160;
    public static final int PACKET_SIZE = UUID_SIZE + MAX_MESSAGE_SIZE;

    // class variables
    private final UUID uniqueId;
    private final String contents;
    private final String sender;
    private final String receiver;
    private final Long sequenceNum;

    /**
     * Constructs a new Bark.
     * 
     * @param contents The contents of the message.
     */
    public Bark(final String contents, final String sender, final String receiver, final Long sequenceNum) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException(
                    "Attempted to create a Bark object with a message larger than the maximum size!" +
                            "\tBark message length:  " + contents.length() + "\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }
        this.uniqueId = UUID.randomUUID();
        this.contents = contents;
        this.sender = sender;
        this.receiver = receiver;
        this.sequenceNum = sequenceNum;
    }

    // public methods

    public String getContents() {
        return this.contents;
    }

    public String getSender() {
        return this.sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public Long getSequenceNum() {
        return this.sequenceNum;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    /**
     * Returns a byte[] containing the bytes which represent the Bark.
     * 
     * @return a byte[] containing the bytes which represent the Bark.
     */
    public byte[] toNetworkBytes() {
        // TODO: Add encryption here.

        return SerializationHelper.serializeObjectToString(this).getBytes();
    }

    /**
     * Returns a Bark derived from the passed byte[].
     * 
     * @return a Bark derived from the passed byte[].
     */
    public static Bark fromNetworkBytes(final byte[] barkBytes) {
        // TODO: Add decrypted here.

        return (Bark) SerializationHelper.deserializeStringToObject(new String(barkBytes));
    }

    // overrides
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Bark)) {
            return false;
        }
        return this.getUniqueId().equals(((Bark) o).getUniqueId());
    }

    @Override
    public String toString() {
        return "Contents:  " + this.contents + "\tuniqueId:  " + this.uniqueId.toString();
    }
}