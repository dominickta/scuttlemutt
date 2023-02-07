package backend.initialization;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.packet.KeyExchangePacket;

import java.security.PublicKey;
import java.util.UUID;

/**
 * This class is used to exchange keys between two connected devices.
 *
 * NOTE:  Since the key exchange process is meant to be exclusively P2P, we don't want them to be repropagated over the
 *        mesh network.  As a result, this process is entirely separate from the MeshDaemon.
 */
public class KeyExchanger {
    private final IOManager ioManager;
    private final StorageManager storageManager;

    /**
     * Constructs the KeyExchanger.
     * @param ioManager  The IOManager used to exchange keys.
     * @param storageManager  A StorageManager used to store the keys received from the other party.
     */
    public KeyExchanger(final IOManager ioManager,
                        final StorageManager storageManager) {
        this.ioManager = ioManager;
        this.storageManager = storageManager;
    }

    /**
     * Used to send the PublicKey to the specified recipient.
     * @param publicKey  The PublicKey being sent.
     * @param recipientId  The ID of the recipient who is receiving the key.  This ID should match the ID value stored
     *                     for the recipient in the IOManager.
     */
    public void sendPublicKey(final PublicKey publicKey, final String recipientId) {
        final KeyExchangePacket kePacket = new KeyExchangePacket(publicKey);
        try {
            this.ioManager.send(recipientId, kePacket);
        } catch (IOManagerException e) {
            // if we fail to send the KeyExchangePacket packet for some reason, an IOManagerException is thrown.
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives a public key from the specified sender, creates a DawgIdentifier for the sender using the key, stores and
     * returns the DawgIdentifier.
     *
     * NOTE:  This call will wait until a key is received from the specified sender.
     * @param senderId  The ID of the sender who we wish to receive a key from.  This ID should match the ID value stored
     *                  for the sender in the IOManager.
     * @return a DawgIdentifier for the specified sender which contains the sender's public key.
     */
    public DawgIdentifier receivePublicKey(final String senderId) {
        // receive the PublicKey.
        final PublicKey publicKey = this.ioManager.singleDeviceReceive(senderId, KeyExchangePacket.class)
                .getPublicKey();

        // create a DawgIdentifier using the PublicKey.  TODO:  Make DawgIdentifier use PublicKey type, _NOT_ String.
        final DawgIdentifier dawgId = new DawgIdentifier(senderId, UUID.randomUUID(), publicKey.toString());

        // store + return the dawgId.
        this.storageManager.storeDawgIdentifier(dawgId);
        return dawgId;
    }
}
