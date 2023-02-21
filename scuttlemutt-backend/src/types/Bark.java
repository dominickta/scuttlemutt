package types;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import crypto.Crypto;

/**
 * Represents a "bark" (message) sent by the user.
 * 
 * All of the bark fields are encrypted. The header is the senders UUID
 * encrypted with the public key of the receiver. Only the receiver can decrypt
 * it to figure out which symmetric keys to use for
 */
public class Bark {
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    // constants

    // stores the maximum number of characters allowed in a Bark.
    public static final int UUID_SIZE = 36;
    public static final int MAX_MESSAGE_SIZE = 160;
    public static final int PACKET_SIZE = UUID_SIZE + MAX_MESSAGE_SIZE;

    /**
     * The unique identifier that is automatially generated when a new Bark is
     * constructed. Two barks with the same fields may not be equal because of
     * their unique ids.
     */
    private final UUID uniqueId;

    /**
     * The UUID of the sender, encrypted with the the receiver's public key.
     */
    private final byte[] encryptedHeader;

    /**
     * The DawgIdentifier of the sender, encrypted with a shared secret key
     * that only the sender and receiver know.
     */
    private final byte[] encryptedSender;

    /**
     * The DawgIdentifier of the receiver, encrypted with a shared secret key
     * that only the sender and receiver know.
     */
    private final byte[] encryptedReceiver;

    /**
     * The order number of this bark, encrypted with a shared secret key
     * that only the sender and receiver know.
     */
    private final byte[] encryptedOrderNum;

    /**
     * The number of filler characters in this bark used to pad the message,
     * encrypted with a shared secret key that only the sender and receiver know.
     */
    private final byte[] encryptedFillerCount;

    /**
     * The string payload of this bark (ie. the actual message), encrypted with
     * a shared secret key that only the sender and receiver know.
     */
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
        this.encryptedHeader = encryptAndSerialize(sender.getUUID(), receiverPublicKey);

        // encrypt the rest with the associated symmetric key
        this.encryptedSender = encryptAndSerialize(sender, encryptionKey);
        this.encryptedReceiver = encryptAndSerialize(receiver, encryptionKey);
        this.encryptedOrderNum = encryptAndSerialize(orderNum, encryptionKey);
        this.encryptedFillerCount = encryptAndSerialize(fillerCount, encryptionKey);
        this.encryptedContents = encryptAndSerialize(contents, encryptionKey);
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
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling any other getter methods.
     * 
     * @param myPrivateKey the private half of my public/private keypair.
     * @return true if this packet is for me, false otherwise
     */
    public boolean isForMe(final PrivateKey myPrivateKey) {
        try {
            return getSenderUUID(myPrivateKey) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the uuid of the sender
     * 
     * @throws Exception if decryption fails (see: Crypto.decrypt)
     * @param myPrivateKey the private key of the current user
     * @return the uuid of the sender
     */
    public UUID getSenderUUID(final PrivateKey myPrivateKey) {
        String keyType = Crypto.ASYMMETRIC_KEY_TYPE;
        byte[] bytes = Crypto.decrypt(this.encryptedHeader, myPrivateKey, keyType);
        return GSON.fromJson(new String(bytes), UUID.class);
    }

    /**
     * Returns the contents of the Bark after decrypting them using the passed
     * in list of secret keys.
     * 
     * This method will also trim off any filler characters used to pad the
     * message. If the decryption fails this will return null.
     * 
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling this method. If a packet is not for you, you will not be
     * able to decrypt it, and this method will throw a RuntimeException.
     * 
     * @throws RuntimeException if decryption fails
     * @param keys A List<SecretKey> to try to decrypt the Bark with.
     * @return The decrypted contents of the Bark as a String, otherwise null.
     */
    public String getContents(final List<SecretKey> keys) {
        // return the decrypted contents with the filler chars trimmed off.
        String content = decryptAndDeserialize(keys, this.encryptedContents, String.class);
        return content.substring(0, content.length() - getFillerCount(keys));
    }

    /**
     * Tries to decrypt the sender field with the list of secret keys. Returns
     * null if that fails.
     * 
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling this method. If a packet is not for you, you will not be
     * able to decrypt it, and this method will throw a RuntimeException.
     * 
     * @throws RuntimeException if decryption fails
     * @param keys the list of secret keys associated with the sender.
     * @return returns the DawgIdentifier of the sender, otherwise null
     */
    public DawgIdentifier getSender(final List<SecretKey> keys) {
        return decryptAndDeserialize(keys, this.encryptedSender, DawgIdentifier.class);
    }

    /**
     * Tries to decrypt the receiver field with the list of secret keys. Returns
     * null if that fails.
     * 
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling this method. If a packet is not for you, you will not be
     * able to decrypt it, and this method will throw a RuntimeException.
     * 
     * @throws RuntimeException if decryption fails
     * @param keys the list of secret keys associated with the sender.
     * @return returns the DawgIdentifier of the receiver, otherwise null
     */
    public DawgIdentifier getReceiver(final List<SecretKey> keys) {
        // TODO: This is tested but never used.
        return decryptAndDeserialize(keys, this.encryptedReceiver, DawgIdentifier.class);
    }

    /**
     * Tries to decrypt the orderNum field with the list of secret keys. Returns
     * null if that fails.
     * 
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling this method. If a packet is not for you, you will not be
     * able to decrypt it, and this method will throw a RuntimeException.
     * 
     * @throws RuntimeException if decryption fails
     * @param keys the list of secret keys associated with the sender.
     * @return returns this Bark's order num, otherwise null
     */
    public Long getOrderNum(final List<SecretKey> keys) {
        return decryptAndDeserialize(keys, this.encryptedOrderNum, Long.class);
    }

    /**
     * Tries to decrypt the fillerCount field with the list of secret keys.
     * Returns null if that fails.
     * 
     * NOTE: The caller should call isForMe with the current user's private key
     * before calling this method. If a packet is not for you, you will not be
     * able to decrypt it, and this method will throw a RuntimeException.
     * 
     * @throws RuntimeException if decryption fails
     * @param keys the list of secret keys associated with the sender.
     * @return returns this Bark's filler count, otherwise null
     */
    public Integer getFillerCount(final List<SecretKey> keys) {
        // TODO: This is only ever used by getContents, consider making private
        return decryptAndDeserialize(keys, this.encryptedFillerCount, Integer.class);
    }

    /**
     * @return the UUID of this Bark
     */
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

    /**
     * A simple helper function to encrypt an object to a JSON string as bytes
     * 
     * @param obj the object to encrypt
     * @param key the key to encrypt the object with
     * @return the bytes of the encrypted object
     */
    private byte[] encryptAndSerialize(Object obj, Key key) {
        String keyType;
        if (key instanceof PublicKey) {
            keyType = Crypto.ASYMMETRIC_KEY_TYPE;
        } else {
            keyType = Crypto.SYMMETRIC_KEY_TYPE;
        }
        return Crypto.encrypt(GSON.toJson(obj).getBytes(), key, keyType);
    }

    /**
     * Try to decrypt the Bark's contents using the passed List of Keys. Since it is
     * most likely that the most recent key is the one used for encryption, we
     * iterate backwards through the List.
     * 
     * @param <T>        The type of object to return if successful
     * @param keys       the list of secret keys to try
     * @param ciphertext the array of bytes to try decrypting
     * @param asType     the class of the type of the object to return
     * @return either an instance of the decrypted object or empty
     */
    private <T> T decryptAndDeserialize(final List<SecretKey> keys, byte[] ciphertext, Class<T> asType) {
        byte[] decryptedMessageBytes = new byte[0];
        for (int i = keys.size() - 1; i >= 0; i--) {
            final SecretKey currentKey = keys.get(i);
            decryptedMessageBytes = Crypto.decrypt(ciphertext, currentKey, Crypto.SYMMETRIC_KEY_TYPE);

            // if bytes is not null, decryption was successful: terminate
            if (decryptedMessageBytes != null) {
                break;
            }
        }

        // if we were never able to successfully decrypt the Bark, return empty
        if (decryptedMessageBytes == null || decryptedMessageBytes.length == 0) {
            throw new RuntimeException("could not decrypt");
        }

        final String decryptedContents = new String(decryptedMessageBytes);
        try {
            return GSON.fromJson(decryptedContents, asType);
        } catch (Exception e) {
            throw new RuntimeException("could not deserialize");
        }
    }
}