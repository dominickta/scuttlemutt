package backend.initialization;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import crypto.Crypto;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.packet.KeyExchangePacket;

/**
 * This class is used to exchange SecretKeys (symmetric keys) between two
 * connected devices.
 *
 * NOTE: Since the key exchange process is meant to be exclusively P2P, we don't
 * want them to be repropagated over the
 * mesh network. As a result, this process is entirely separated from the
 * MeshDaemon.
 * 
 * The key exchange handshake will need to first exchange public keys and then
 * exchange secret keys. So: sendPublicKey, receivePublicKey, sendSecretKey,
 * then receiveSecretKey.
 */
public class KeyExchanger {
    private final IOManager ioManager;
    private final StorageManager storageManager;

    /**
     * Scuttlemutt's SecretKey exchange mechanism works as follows:
     * - Establish encrypted connection between the two devices. (not done here)
     * - Generate a SecretKey for the connection + send it to the other device.
     * - Once you receive the SecretKey from the other device, hash the two keys.
     * Whichever
     * key has a greater value, keep that one. This allows both devices to converge
     * upon one key.
     *
     * The keysBeingExchanged Map maintains records of the locally-generated keys
     * for which we're
     * currently exchanging. Once the exchange is completed, the local key is
     * removed from the Map.
     */
    private final Map<String, SecretKey> secretKeysBeingExchanged;

    /**
     * Constructs the KeyExchanger.
     * 
     * @param ioManager      The IOManager used to exchange keys.
     * @param storageManager A StorageManager used to store the keys received from
     *                       the other party.
     */
    public KeyExchanger(final IOManager ioManager,
            final StorageManager storageManager) {
        this.ioManager = ioManager;
        this.storageManager = storageManager;
        this.secretKeysBeingExchanged = new HashMap<String, SecretKey>();
    }

    // send our public key to the recipient
    public void sendPublicKey(final String recipientId, final PublicKey myPublicKey) {
        final KeyExchangePacket keyPacket = new KeyExchangePacket(myPublicKey);
        try {
            this.ioManager.send(recipientId, keyPacket);
        } catch (IOManagerException e) {
            // if we fail to send the KeyExchangePacket packet for some reason, an
            // IOManagerException is thrown.
            throw new RuntimeException(e);
        }
    }

    // receive a public key from the sender and store it in the storage manager
    public PublicKey receivePublicKey(final String senderId) {
        PublicKey publicKey = (PublicKey) this.ioManager
                .singleDeviceReceive(senderId, KeyExchangePacket.class)
                .getKey();
        this.storageManager.storePublicKeyForDeviceId(senderId, publicKey);
        return publicKey;
    }

    /**
     * Used to send a SecretKey to the specified recipient.
     *
     * Both ends of the exchange process must send a SecretKey in order to complete
     * the process.
     *
     * @param recipientId The ID of the recipient who is receiving the key. This ID
     *                    should match the ID value stored
     *                    for the recipient in the IOManager.
     */
    public void sendSecretKey(final String recipientId) {
        final SecretKey secretKey = Crypto.generateSecretKey();
        final KeyExchangePacket kePacket = new KeyExchangePacket(secretKey);
        try {
            this.ioManager.send(recipientId, kePacket);
        } catch (IOManagerException e) {
            // if we fail to send the KeyExchangePacket packet for some reason, an
            // IOManagerException is thrown.
            throw new RuntimeException(e);
        }

        // store the SecretKey for later usage when receiving the key from the other
        // party.
        this.secretKeysBeingExchanged.put(recipientId, secretKey);
    }

    /**
     * Receives a secret (symmetric) key from the specified sender, creates a
     * DawgIdentifier for the sender using the key, stores and
     * returns the DawgIdentifier.
     *
     * NOTE: This call will wait until a key is received from the specified sender.
     * 
     * @param senderId The ID of the sender who we wish to receive a key from. This
     *                 ID should match the ID value stored
     *                 for the sender in the IOManager.
     * @return a DawgIdentifier for the specified sender which contains the sender's
     *         public key.
     */
    public DawgIdentifier receiveSecretKey(final String senderId, final PublicKey senderPublicKey) {
        // receive the SecretKey.
        final SecretKey otherSecretKey = (SecretKey) this.ioManager
                .singleDeviceReceive(senderId, KeyExchangePacket.class)
                .getKey();

        // create a DawgIdentifier to represent the other party.
        final DawgIdentifier dawgId = new DawgIdentifier(senderId, senderPublicKey);

        // at this point, we have keys from both parties. let's determine which one
        // should be used
        // for the connections by hashing them and choosing the one with the
        // higher-value.
        final SecretKey localSecretKey = this.secretKeysBeingExchanged.get(senderId);
        final SecretKey chosenKey = localSecretKey.hashCode() < otherSecretKey.hashCode() ? otherSecretKey
                : localSecretKey;

        // store the chosenKey.
        this.storageManager.storeSecretKeyForPublicKey(dawgId.getPublicKey(), chosenKey);

        // store + return the dawgId.
        this.storageManager.storeDawgIdentifier(dawgId);
        return dawgId;
    }
}
