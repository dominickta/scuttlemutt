package backend.initialization;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import crypto.Crypto;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.packet.KeyExchangePacket;

import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

/**
 * This class is used to exchange symmetric and asymmetric keys between
 * two connected devices over a secure channel.
 *
 * NOTE: Since the key exchange process is meant to be exclusively P2P, we don't
 * want them to be repropagated over the mesh network. As a result, this process
 * is entirely separated from the MeshDaemon.
 */
public class KeyExchanger {
    private final IOManager ioManager;
    private final StorageManager storageManager;

    /**
     * Scuttlemutt's key exchange mechanism works as follows:
     * - Establish encrypted connection between the two devices. (not done here)
     * - Read out our own public key, and send it to the other device
     * - Generate a SecretKey for the connection + send it to the other device.
     * - Once you receive the SecretKey from the other device, hash the two keys.
     * Whichever key has a greater value, keep that one. This allows both devices to
     * converge upon one key.
     *
     * The secretKeysBeingExchanged map maintains records of the locally-generated
     * keys for which we're currently exchanging. Once the exchange is completed,
     * the local key is removed from the Map.
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

    /**
     * Exchanges a PublicKey and a SecretKey between two parties. The parties
     * each store the received public key and they converge on the same secret
     * key. This must be done over a secure channel.
     *
     * The sender must be able to decrypt messages encrypted using the public
     * key sent over this exchange.
     *
     * @param myPublicKey The public key of the sender. This will be saved on the
     *                    receiving end and used to encrypt messages sent to the
     *                    sender.
     * @param recipientName The name of the recipient who is recieving the key
     * @param dawgId The dawgIdentifier of the sender
     */
    public void sendKeys(final String recipientName, final PublicKey myPublicKey, DawgIdentifier dawgId) {
        final SecretKey secretKey = Crypto.generateSecretKey();
        final KeyExchangePacket packet = new KeyExchangePacket(myPublicKey, secretKey, dawgId);
        try {
            this.ioManager.send(recipientName, packet);
        } catch (IOManagerException e) {
            // if we fail to send the KeyExchangePacket packet for some reason, an
            // IOManagerException is thrown.
            throw new RuntimeException(e);
        }

        // store the SecretKey for later usage (ie. in `receiveKeys`)
        this.secretKeysBeingExchanged.put(recipientName, secretKey);
    }


    public DawgIdentifier receiveSecretKeyForNewContact(final String senderName, KeyExchangePacket packet) {
        final DawgIdentifier senderDawgId = packet.getDawgId();
        this.storageManager.storeDawgIdentifier(senderDawgId);
        return this.receiveSecretKey(senderDawgId, packet);
    }


    /**
     * Receives a secret (symmetric) key from the specified sender, creates a
     * DawgIdentifier for the sender using the key, stores and
     * returns the DawgIdentifier.
     *
     * NOTE:  This call will wait until a key is received from the specified sender.
     * @param dawgId  The DawgIdentifier of the sender who we wish to receive a key from.  This ID should match the ID value stored
     *                  for the sender in the IOManager.
     * @return a DawgIdentifier for the specified sender which contains the sender's public key.
     */
    public DawgIdentifier receiveSecretKey(final DawgIdentifier dawgId, KeyExchangePacket packet) {
        // receive the SecretKey.
        final SecretKey otherSecretKey = (SecretKey) packet.getSecretKey();
        PublicKey senderPublicKey = packet.getPublicKey();
        SecretKey senderSecretKey = packet.getSecretKey();
        // at this point, we have keys from both parties.  let's determine which one should be used
        // for the connections by hashing them and choosing the one with the higher-value.
        final SecretKey localSecretKey = this.secretKeysBeingExchanged.get(dawgId.getUsername());
        final SecretKey chosenKey = localSecretKey.hashCode() < otherSecretKey.hashCode() ? otherSecretKey : localSecretKey;


        this.secretKeysBeingExchanged.remove(dawgId.getUsername());

        // store the keys
        this.storageManager.storePublicKeyForUUID(dawgId.getUUID(), senderPublicKey);
        this.storageManager.storeSecretKeyForUUID(dawgId.getUUID(), chosenKey);
        this.storageManager.storeDawgIdentifier(dawgId);

        // return the senders dawgIdentifier
        return dawgId;
    }
}
