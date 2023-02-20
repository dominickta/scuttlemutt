package types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import crypto.Crypto;

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
    private final byte[] encryptedContents;
    private final int fillerCount;  // stores the number of "filler" (dummy) chars filling up the buf.  This is always
                                    // at the end of the String.
    private final DawgIdentifier sender;
    private final DawgIdentifier receiver;
    private final Long orderNum;

    /**
     * Constructs a new Bark.
     *
     * @param contents The contents of the message.
     * @param sender   The DawgIdentifier of the sender of the message.
     * @param receiver The DawgIdentifier of the receiver of the message.
     * @param orderNum The number of the message in the conversation order.
     * @param encryptionKey  The symmetric SecretKey used to encrypt the contents of the Bark.
     */
    public Bark(String contents,
                final DawgIdentifier sender,
                final DawgIdentifier receiver,
                final Long orderNum,
                final SecretKey encryptionKey) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException(
                    "Attempted to create a Bark object with a message larger than the maximum size!" +
                            "\tBark message length:  " + contents.length() + "\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }
        this.uniqueId = UUID.randomUUID();

        // setup the filler chars.
        this.fillerCount = MAX_MESSAGE_SIZE - contents.length();
        contents += RandomStringUtils.randomAlphanumeric(this.fillerCount);

        // encrypt the contents.
        this.encryptedContents = Crypto.encrypt(contents.getBytes(), encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
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
        this.encryptedContents = bark.encryptedContents;
        this.sender = bark.sender;
        this.receiver = bark.receiver;
        this.orderNum = bark.orderNum;
    }

    // public methods

    /**
     * Returns the contents of the Bark after decrypting them using the passed key.
     * @param encryptionKeyList  A List<Key> of Key objects to try to decrypt the Bark's contents with.
     * @return  An Optional containing the decrypted contents of the Bark.  If the Bark was unable
     *          to be successfully decrypted, return Optional.empty().
     */
    public Optional<String> getContents(final List<Key> encryptionKeyList) {
        // try to decrypt the Bark's contents using the passed List of Keys.
        // since it is most likely that the most recent key is the one used for encryption, we iterate
        // backwards through the List.
        byte[] decryptedMessageBytes = new byte[0];
        for (int i = encryptionKeyList.size() - 1; i >= 0; i--) {
            final Key currentKey = encryptionKeyList.get(i);

            // if the currentKey is not a SymmetricKey for some reason, throw an exception.
            if (!(currentKey instanceof SecretKey)) {
                throw new RuntimeException("Attempted to decrypt Bark contents with a Key type which is not a SecretKey.");
            }

            // attempt decryption.
            decryptedMessageBytes = Crypto.decrypt(this.encryptedContents, currentKey, Crypto.SYMMETRIC_KEY_TYPE);

            // if decryptedMessageBytes is not null, the decryption was successful and we should terminate the loop.
            if (decryptedMessageBytes != null) {
                break;
            }
        }

        // if we were never able to successfully decrypt the Bark, return Optional.empty().
        if (decryptedMessageBytes == null || decryptedMessageBytes.length == 0) {
            return Optional.empty();
        }

        final String decryptedContents = new String(decryptedMessageBytes);

        // return the decrypted contents with the filler chars trimmed off.
        return Optional.of(decryptedContents.substring(0, decryptedContents.length() - this.fillerCount));
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
        return "encryptedContents:  " + Arrays.toString(this.encryptedContents) + "\tuniqueId:  " + this.uniqueId.toString();
    }
}