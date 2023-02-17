package types;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import crypto.Crypto;

/**
 * Represents the metadata on top of a Bark.
 */
public class BarkHeader {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // class variables
    private final DawgIdentifier sender;
    private final DawgIdentifier receiver;
    private final Long orderNum;
    private final int fillerCount; // stores the number of "filler" (dummy) chars filling up the buf. This is
                                   // always at the end of the String.

    /**
     * Constructs a new BarkHeader.
     *
     * @param contents      The contents of the message.
     * @param sender        The DawgIdentifier of the sender of the message.
     * @param receiver      The DawgIdentifier of the receiver of the message.
     * @param orderNum      The number of the message in the conversation order.
     * @param encryptionKey The asymmetric PrivateKey used to encrypt the contents
     *                      of the BarkHeader.
     */
    public BarkHeader(final DawgIdentifier sender,
            final DawgIdentifier receiver,
            final Long orderNum,
            final int contentLength) {
        this.sender = sender;
        this.receiver = receiver;
        this.orderNum = orderNum;
        this.fillerCount = Bark.MAX_MESSAGE_SIZE - contentLength;
    }

    // public methods

    public DawgIdentifier getSender() {
        return this.sender;
    }

    public DawgIdentifier getReceiver() {
        return this.receiver;
    }

    public Long getOrderNum() {
        return this.orderNum;
    }

    public int getFillerCount() {
        return this.fillerCount;
    }

    /**
     * Returns a byte[] containing the bytes which represent the BarkHeader.
     *
     * @return a byte[] containing the bytes which represent the BarkHeader.
     */
    public byte[] toEncryptedBytes(PublicKey publicKey) {
        byte[] rawBytes = GSON.toJson(this).getBytes(StandardCharsets.UTF_8);
        return Crypto.encrypt(rawBytes, publicKey, Crypto.ASYMMETRIC_KEY_TYPE);
    }

    /**
     * Returns a BarkHeader derived from the passed byte[].
     *
     * @return a BarkHeader derived from the passed byte[].
     */
    public static BarkHeader fromEncryptedBytes(final byte[] barkHeaderBytes, final PrivateKey privateKey) {
        try {
            byte[] decryptedBytes = Crypto.decrypt(barkHeaderBytes, privateKey, Crypto.ASYMMETRIC_KEY_TYPE);
            return GSON.fromJson(new String(decryptedBytes, StandardCharsets.UTF_8), BarkHeader.class);
        } catch (Exception e) {
            // either decryption or json deserialization failed
            e.printStackTrace();
            return null;
        }
    }
}