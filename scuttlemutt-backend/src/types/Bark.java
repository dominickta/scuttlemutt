package types;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import crypto.Crypto;

/**
 * Represents a "bark" (message) sent by the user.
 * 
 * The Bark stores the following bytes:
 * [ ~16 byte UUID ][ ~32 byte header ][ ~1000 byte payload ]
 * 
 * The Bark itself is not encrypted, the Bark’s contents are encrypted — the
 * header can be decrypted with each device’s public key, and the contents can
 * be decrypted by using the AES key associated with the sender inside the
 * header, which you can only get at if you can decrypt the header. Only the
 * sender and recipient know the AES key they use to send messages.
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
    private final byte[] encryptedHeader;
    private final byte[] encryptedSender;
    private final byte[] encryptedReceiver;
    private final byte[] encryptedOrderNum;
    private final byte[] encryptedFillerCount;
    private final byte[] encryptedContents;

    /**
     * Constructs a new Bark.
     *
     * @param contents          The contents of the message.
     * @param sender            The DawgIdentifier of the sender of the message.
     * @param receiver          The DawgIdentifier of the receiver of the message.
     * @param orderNum          The number of the message in the conversation order.
     * @param receiverPublicKey The public key of the receiver.
     * @param encryptionKey     The symmetric SecretKey used to encrypt the contents
     *                          of the Bark.
     */
    public Bark(String contents,
            final DawgIdentifier sender,
            final DawgIdentifier receiver,
            final Long orderNum,
            final PublicKey receiverPublicKey,
            final SecretKey encryptionKey) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException(
                    "Attempted to create a Bark object with a message larger than the maximum size!" +
                            "\tBark message length:  " + contents.length() + "\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }
        this.uniqueId = UUID.randomUUID();

        // setup the filler chars.
        int fillerCount = MAX_MESSAGE_SIZE - contents.length();
        contents += RandomStringUtils.randomAlphanumeric(fillerCount);

        // encrypt the uuid with an asymmetric key (small size limit)
        this.encryptedHeader = Crypto.encrypt(GSON.toJson(sender.getUUID()).getBytes(), receiverPublicKey,
                Crypto.ASYMMETRIC_KEY_TYPE);

        // encrypt the rest with the associated symmetric key
        this.encryptedSender = Crypto.encrypt(GSON.toJson(sender).getBytes(), encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        this.encryptedReceiver = Crypto.encrypt(GSON.toJson(receiver).getBytes(), encryptionKey,
                Crypto.SYMMETRIC_KEY_TYPE);
        this.encryptedOrderNum = Crypto.encrypt(GSON.toJson(orderNum).getBytes(), encryptionKey,
                Crypto.SYMMETRIC_KEY_TYPE);
        this.encryptedFillerCount = Crypto.encrypt(GSON.toJson(fillerCount).getBytes(), encryptionKey,
                Crypto.SYMMETRIC_KEY_TYPE);
        this.encryptedContents = Crypto.encrypt(contents.getBytes(), encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
    }

    /**
     * Constructs a copy of a Bark.
     *
     * @param bark The Bark to copy.
     */
    public Bark(Bark bark) {
        this.uniqueId = bark.uniqueId;
        this.encryptedHeader = bark.encryptedHeader;
        this.encryptedSender = bark.encryptedSender;
        this.encryptedReceiver = bark.encryptedReceiver;
        this.encryptedOrderNum = bark.encryptedOrderNum;
        this.encryptedFillerCount = bark.encryptedFillerCount;
        this.encryptedContents = bark.encryptedContents;
    }

    // public methods

    /**
     * The Bark object is for me if I can decrypt the BarkHeader with my
     * private (asymmetric) key.
     * 
     * @param myPrivateKey the private half of my public/private keypair.
     * @return true if this packet is for me, false otherwise
     */
    public boolean isForMe(final PrivateKey myPrivateKey) {
        try {
            byte[] bytes = Crypto.decrypt(this.encryptedHeader, myPrivateKey, Crypto.ASYMMETRIC_KEY_TYPE);
            UUID id = GSON.fromJson(new String(bytes), UUID.class);
            return id != null;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

    /**
     * Returns the contents of the Bark after decrypting them using the passed key.
     * 
     * @param encryptionKey The key to decrypt the Bark's contents with.
     * @return The decrypted contents of the Bark or null if you dont have
     *         permission.
     */
    public String getContents(final PrivateKey myPrivateKey, final SecretKey encryptionKey) {
        if (!isForMe(myPrivateKey)) {
            return null;
        }

        // decrypt the Bark's contents.
        final byte[] rawBytes = Crypto.decrypt(this.encryptedContents, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        final String decryptedContents = new String(rawBytes);

        // return the decrypted contents with the filler chars trimmed off.
        int fillerCount = getFillerCount(myPrivateKey, encryptionKey);
        return decryptedContents.substring(0, decryptedContents.length() - fillerCount);
    }

    public DawgIdentifier getSender(final PrivateKey myPrivateKey, final SecretKey encryptionKey) {
        if (!isForMe(myPrivateKey)) {
            return null;
        }

        // decrypt the Bark's contents.
        final byte[] rawBytes = Crypto.decrypt(this.encryptedSender, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        return GSON.fromJson(new String(rawBytes), DawgIdentifier.class);
    }

    public DawgIdentifier getReceiver(final PrivateKey myPrivateKey, final SecretKey encryptionKey) {
        if (!isForMe(myPrivateKey)) {
            return null;
        }

        // decrypt the Bark's contents.
        final byte[] rawBytes = Crypto.decrypt(this.encryptedReceiver, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        return GSON.fromJson(new String(rawBytes), DawgIdentifier.class);
    }

    public Long getOrderNum(final PrivateKey myPrivateKey, final SecretKey encryptionKey) {
        if (!isForMe(myPrivateKey)) {
            return null;
        }

        // decrypt the Bark's contents.
        final byte[] rawBytes = Crypto.decrypt(this.encryptedOrderNum, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        return GSON.fromJson(new String(rawBytes), Long.class);
    }

    public Integer getFillerCount(final PrivateKey myPrivateKey, final SecretKey encryptionKey) {
        if (!isForMe(myPrivateKey)) {
            return null;
        }

        // decrypt the Bark's contents.
        final byte[] rawBytes = Crypto.decrypt(this.encryptedFillerCount, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
        return GSON.fromJson(new String(rawBytes), Integer.class);
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
        return GSON.toJson(this).getBytes();
    }

    /**
     * Returns a Bark derived from the passed byte[].
     *
     * @return a Bark derived from the passed byte[].
     */
    public static Bark fromNetworkBytes(final byte[] barkBytes) {
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
        return "encryptedContents:  " + Arrays.toString(this.encryptedContents) + "encryptedHeader:  "
                + Arrays.toString(this.encryptedHeader) + "\tuniqueId:  "
                + this.uniqueId.toString();
    }
}