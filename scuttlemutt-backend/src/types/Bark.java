package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

/**
 * Represents a "bark" (message) sent by the user.
 */
public class Bark {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    // constants

    // stores the maximum number of characters allowed in a Bark.
    public static final int UUID_SIZE = 36;
    public static final int MAX_MESSAGE_SIZE = 160;
    public static final int PACKET_SIZE = UUID_SIZE + MAX_MESSAGE_SIZE;


    // class variables
    private final UUID uniqueId;
    private final String contents;
    private final int fillerCount;  // stores the number of "filler" (dummy) chars filling up the buf.  This is always
                                    // at the end of the String.
    private final DawgIdentifier sender;
    private final DawgIdentifier receiver;
    private final Long orderNum;

    /**
     * Constructs a new Bark.
     *
     * @param contents The contents of the message.
     * @param sender The DawgIdentifier of the sender of the message.
     * @param receiver The DawgIdentifier of the receiver of the message.
     * @param orderNum The number of the message in the conversation order.
     */
    public Bark(final String contents,
                final DawgIdentifier sender,
                final DawgIdentifier receiver,
                final Long orderNum) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException(
                    "Attempted to create a Bark object with a message larger than the maximum size!" +
                            "\tBark message length:  " + contents.length() + "\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }
        this.uniqueId = UUID.randomUUID();

        // setup the filler chars.
        this.fillerCount = MAX_MESSAGE_SIZE - contents.length();
        this.contents = contents + RandomStringUtils.randomAlphanumeric(this.fillerCount);
        this.sender = sender;
        this.receiver = receiver;
        this.orderNum = orderNum;
    }

    /**
     * Constructs a copy of a Bark.
     *
     * @param bark The Bark to copy.
     */
    public Bark(Bark bark) {
        this.uniqueId = bark.uniqueId;
        this.fillerCount = bark.fillerCount;
        this.contents = bark.contents;
        this.sender = bark.sender;
        this.receiver = bark.receiver;
        this.orderNum = bark.orderNum;
    }

    // public methods

    public String getContents() {
        // when returning the contents, trim off the filler chars.
        return this.contents.substring(0, this.contents.length() - this.fillerCount);
    }

    public DawgIdentifier getSender() {
        return this.sender;
    }

    public DawgIdentifier getReceiver() {
        return this.receiver;
    }

    public Long getOrderNum() {
        return this.orderNum;
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
        // TODO:  Add encryption here.

        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a Bark derived from the passed byte[].
     *
     * @return a Bark derived from the passed byte[].
     */
    public static Bark fromNetworkBytes(final byte[] barkBytes) {
        // TODO:  Add decryption here.
        return GSON.fromJson(new String(barkBytes), Bark.class);
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
    public int hashCode() {
        return this.getUniqueId().hashCode();
    }

    @Override
    public String toString() {
        return "Contents:  " + this.contents + "\tuniqueId:  " + this.uniqueId.toString();
    }
}