package types;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.google.common.primitives.Bytes;
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
    // constants
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // stores the maximum number of characters allowed in a Bark.
    public static final int MAX_MESSAGE_SIZE = 160;

    // private fields
    /**
     * The unique identifier that is automatically generated when a new Bark is
     * constructed. Two barks with the same fields may not be equal because of
     * their unique ids.
     */
    private final UUID uniqueId;

    /**
     * The UUID of the sender, encrypted with the receiver's public key.
     */
    private final byte[] encryptedHeader;

    /**
     * The bark payload, signed with the sender's private key, appended to the
     * bark header bytes, then encrypted with the shared secret key.
     */
    private final byte[] encryptedPayload;

    /**
     * Constructs a new Bark.
     *
     * @param contents          The contents of the message.
     * @param sender            The DawgIdentifier of the sender of the message.
     * @param orderNum          The number of the message in the conversation order.
     * @param receiverPublicKey The public key of the receiver.
     * @param encryptionKey     The symmetric SecretKey used to encrypt the contents
     *                          of the Bark.
     */
    public Bark(String contents,
            final DawgIdentifier sender,
            final Long orderNum,
            final PrivateKey senderPrivateKey,
            final PublicKey receiverPublicKey,
            final SecretKey encryptionKey) {

        // verify that the contents are less than the max message size.
        if (contents.length() > MAX_MESSAGE_SIZE) {
            throw new RuntimeException(
                    "Attempted to create a Bark object with a message larger " +
                    "than the maximum size!\tBark message length:  " +
                    contents.length() + "\tMaximum size:  " + MAX_MESSAGE_SIZE);
        }
        this.uniqueId = UUID.randomUUID();

        // encrypt the uuid with an asymmetric key (small size limit)
        byte[] headerBytes = GSON.toJson(sender.getUUID()).getBytes();
        this.encryptedHeader = Crypto.encrypt(headerBytes, receiverPublicKey, Crypto.ASYMMETRIC_KEY_TYPE);

        // construct the bark payload, sign the raw header bytes, encrypt both with the SecretKey.
        byte[] payload = new BarkPayload(contents, sender, orderNum).toNetworkBytes();
        byte[] payloadSignature = Crypto.sign(payload, senderPrivateKey);
        byte[] bytes = Bytes.concat(payload, payloadSignature);
        this.encryptedPayload = Crypto.encrypt(bytes, encryptionKey, Crypto.SYMMETRIC_KEY_TYPE);
    }

    /**
     * Constructs a copy of a Bark.
     *
     * @param bark The Bark to copy.
     */
    public Bark(Bark bark) {
        this.uniqueId = bark.uniqueId;
        this.encryptedHeader = bark.encryptedHeader;
        this.encryptedPayload = bark.encryptedPayload;
    }

    // public methods

    /**
     * The Bark object is for me if I can decrypt the header with my
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
     * @param secretKeys the list of secret keys associated with the sender.
     * @param publicKey the sender's public key (used to verify signature)
     * @return The decrypted contents of the Bark as a String, otherwise null.
     */
    public String getContents(final List<SecretKey> secretKeys, final PublicKey publicKey) {
        return decryptBarkPayload(secretKeys, publicKey).getContents();
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
     * @param secretKeys the list of secret keys associated with the sender.
     * @param publicKey the sender's public key (used to verify signature)
     * @return returns the DawgIdentifier of the sender, otherwise null
     */
    public DawgIdentifier getSender(final List<SecretKey> secretKeys, final PublicKey publicKey) {
        return decryptBarkPayload(secretKeys, publicKey).getSender();
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
     * @param secretKeys the list of secret keys associated with the sender.
     * @param publicKey the sender's public key (used to verify signature)
     * @return returns this Bark's order num, otherwise null
     */
    public Long getOrderNum(final List<SecretKey> secretKeys, final PublicKey publicKey) {
        return decryptBarkPayload(secretKeys, publicKey).getOrderNum();
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
        return "encryptedPayload:  " + Arrays.toString(this.encryptedPayload)
                + "encryptedHeader:  " + Arrays.toString(this.encryptedHeader)
                + "\tuniqueId:  " + this.uniqueId.toString();
    }

    // private helpers
    /**
     * Try to decrypt the Bark's payload using the passed List of Keys. Since it
     * is most likely that the most recent key is the one used for encryption, we
     * iterate backwards through the List.
     * 
     * For security, we only return the payload if the attached signature is
     * valid for the given public key.
     * 
     * @throws RuntimeException if decryption fails or the signature is invalid
     * @param keys       the list of secret keys to try decrypting with
     * @param publicKey  the public key of the sender, should match signature
     * @return either an instance of BarkPayload or throws
     */
    private BarkPayload decryptBarkPayload(final List<SecretKey> keys, PublicKey publicKey) {
        byte[] bytes = new byte[0];
        for (int i = keys.size() - 1; i >= 0; i--) {
            final SecretKey key = keys.get(i);
            bytes = Crypto.decrypt(this.encryptedPayload, key, Crypto.SYMMETRIC_KEY_TYPE);

            // if bytes is not null, decryption was successful: terminate
            if (bytes != null) {
                break;
            }
        }

        // if we were never able to successfully decrypt the Bark, return empty
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("could not decrypt");
        }

        int sigIndex = bytes.length - Crypto.ASYMMETRIC_KEY_SIZE / 8;
        byte[] payload = Arrays.copyOfRange(bytes, 0, sigIndex);        
        byte[] payloadSignature = Arrays.copyOfRange(bytes, sigIndex, bytes.length); 
        if (Crypto.verify(payloadSignature, payload, publicKey)) {
            return BarkPayload.fromNetworkBytes(payload);
        }
        throw new RuntimeException("invalid signature");
    }
}