package backend.scuttlemutt;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import backend.initialization.KeyExchangeDaemon;
import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;
import crypto.Crypto;
import storagemanager.StorageManager;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

/*
 * This class contains and organizes the content necessary to run the local device's node on the Scuttlemutt network.
 */
public class Scuttlemutt {

    // Identifier object of user
    private final DawgIdentifier dawgIdentifier;
    // I/O manager
    public final IOManager ioManager;
    // Database of connected users, previous conversations, and previous messages
    private final StorageManager storageManager;
    // Daemon controlling recieving and broadcasting of messages
    private final MeshDaemon meshDaemon;
    // Daemon used to exchange keys between devices.
    private final KeyExchangeDaemon keyExchangeDaemon;

    /*
     * Constructs a new Scuttlemutt object
     */
    public Scuttlemutt(IOManager inputIoManager, DawgIdentifier dawgIdentifier, StorageManager storageManager) {
        this.dawgIdentifier = dawgIdentifier;
        this.ioManager = inputIoManager;
        this.storageManager = storageManager;
        // only generate a new keypair if we don't already have one
        if (getPrivateKey() == null) {
            // create a keypair and store them in the storage manager
            KeyPair keys = Crypto.generateKeyPair();
            this.storageManager.storePrivateKey(keys.getPrivate());
            this.storageManager.storePublicKeyForUUID(dawgIdentifier.getUUID(), keys.getPublic());
        }

        // local keys must be initialized before the mesh daemon is constructed.
        this.meshDaemon = new MeshDaemon(this.ioManager, this.storageManager, this.dawgIdentifier);

        // initialize KeyExchangeDaemon.
        this.keyExchangeDaemon = new KeyExchangeDaemon(this.ioManager, this.storageManager, this.getPublicKey(), this.dawgIdentifier);
    }

    /**
     * Returns user's DawgIdentifier
     *
     * @return deep copy of user's DawgIdentifier so public key can't be modified
     */
    public DawgIdentifier getDawgIdentifier() {
        return new DawgIdentifier(this.dawgIdentifier.getUsername(), this.dawgIdentifier.getUUID());
    }

    /**
     * Returns the SecretKey used to chat with the device associated with the passed
     * DawgIdentifier.
     *
     * @param otherDeviceId A DawgIdentifier used to identify the other device.
     * @return the SecretKey used to chat with the device associated with the passed
     *         DawgIdentifier.
     */
    public List<SecretKey> getSecretKeys(final DawgIdentifier otherDeviceId) {
        return this.storageManager.lookupSecretKeysForUUID(otherDeviceId.getUUID());
    }

    /**
     * Returns the PrivateKey of the current device.
     */
    public PrivateKey getPrivateKey() {
        return this.storageManager.lookupPrivateKey();
    }

    /**
     * Returns the PublicKey of the current device.
     */
    public PublicKey getPublicKey() {
        return this.storageManager.lookupPublicKeyForUUID(this.dawgIdentifier.getUUID());
    }

    /**
     * Returns the latest SecretKey on record used to chat with the device
     * associated with the passed DawgIdentifier.
     *
     * @param otherDeviceId A DawgIdentifier used to identify the other device.
     * @return the SecretKey used to chat with the device associated with the passed
     *         DawgIdentifier.
     */
    public SecretKey getLatestSecretKey(final DawgIdentifier otherDeviceId) {
        // get a List of the most recently known Keys.
        final List<SecretKey> knownKeys = this.getListOfHistoricalKeys(otherDeviceId);

        // obtain the last (and therefore newest) Key in the List.
        return knownKeys.get(knownKeys.size() - 1);
    }

    /**
     * Returns a List<Key> containing some of the Keys historically known to have
     * been used to chat with the device associated with the passed DawgIdentifier.
     *
     * @param otherDeviceId A DawgIdentifier used to identify the other device.
     * @return a List<SecretKey> containing some of the Keys historically known to
     *         have been used to chat with the device associated with the passed
     *         DawgIdentifier.
     */
    public List<SecretKey> getListOfHistoricalKeys(final DawgIdentifier otherDeviceId) {
        return this.storageManager.lookupSecretKeysForUUID(otherDeviceId.getUUID());
    }

    /**
     * Sends a message to recipient based on UUID
     *
     * @param message   Message that user is trying to send
     * @param dstDawgId Recipient's DawgIdentifier
     * @return UUID of sent message
     */
    public UUID sendMessage(String message, DawgIdentifier dstDawgId) {
        // compute the sequenceId for the message.
        final Conversation c = this.getConversation(dstDawgId);
        final long seqId;
        if (c == null) {
            seqId = 0;
        } else {
            seqId = this.getMessagesForConversation(c).size();
        }

        return this.meshDaemon.sendMessage(message, dstDawgId, seqId);
    }

    /**
     * Returns a List containing the active conversations for the user.
     *
     * @return a List containing the active conversations for the user.
     */
    public List<Conversation> listAllConversations() {
        return this.storageManager.listAllConversations();
    }

    /**
     * Returns a List<String> containing the messages of the passed Conversation.
     *
     * @param conversation the Conversation whose messages we're obtaining.
     * @return a List<Message> containing the messages of the passed Conversation.
     */
    public List<Message> getMessagesForConversation(final Conversation conversation) {
        final List<Message> messages = new ArrayList<Message>();
        for (final UUID messageUuid : conversation.getMessageUUIDList()) {
            final Message m = this.storageManager.lookupMessage(messageUuid);

            // we only store a message if one is found, otherwise we can just
            // fail silently since there's no way to get a message we don't have.
            if (m != null) {
                messages.add(m);
            }
        }
        return messages;
    }

    public List<DawgIdentifier> getAllContacts() {
        return this.storageManager.getAllDawgIdentifiers();
    }

    public boolean haveContact(final UUID uuid) {
        return this.storageManager.lookupDawgIdentifierForUuid(uuid) != null;
    }

    public DawgIdentifier getContactDawgId(final String senderId) {
        return this.storageManager.lookupDawgIdentifierForUsername(senderId);
    }

    /**
     * Exchanges keys with the specified user and stores a reference to it.
     *
     * @param otherDeviceId  The ID of the device with which we're exchanging keys.  The ID
     *                       should be understandable to the IOManager.
     */
    public void exchangeKeys(final String otherDeviceId) {
        this.keyExchangeDaemon.exchangeKeys(otherDeviceId);
    }

    /**
     * Returns the current status for the Key exchange process with the device associated with the
     * specified device ID String.
     * @param otherDeviceId  The ID of the device with which we're looking up the exchange status
     *                       for.  The ID should be understandable to the IOManager.
     * @return  The current status for the Key exchange process with the specified device.
     */
    public KeyExchangeDaemon.KEY_EXCHANGE_STATUS getKeyExchangeStatus(final String otherDeviceId) {
        return this.keyExchangeDaemon.getKeyExchangeStatus(otherDeviceId);
    }

    // TODO:  Delete this method when after integrating KeyExchangeDaemon.
    public void addContact(final DawgIdentifier dawgIdentifier, final PublicKey publicKey, final SecretKey secretKey) {
        this.storageManager.storeDawgIdentifier(dawgIdentifier);
        this.storageManager.storePublicKeyForUUID(dawgIdentifier.getUUID(), publicKey);
        this.storageManager.storeSecretKeyForUUID(dawgIdentifier.getUUID(), secretKey);
    }

    public void removeContact(final DawgIdentifier dawgIdentifier) {
        // verify that the DawgIdentifier is currently stored.
        if (this.storageManager.lookupDawgIdentifierForUuid(dawgIdentifier.getUUID()) == null) {
            String msg = "Attempted to delete a nonexistent DawgIdentifier! ";
            throw new RuntimeException(msg + this.dawgIdentifier.toString());
        }

        this.storageManager.deleteDawgIdentifierByUuid(dawgIdentifier.getUUID());
        this.storageManager.deletePublicKeyForUUID(dawgIdentifier.getUUID());
        this.storageManager.deleteSecretKeysForUUID(dawgIdentifier.getUUID());
    }

    public Conversation getConversation(final DawgIdentifier dawgIdentifier) {
        return this.storageManager.lookupConversation(dawgIdentifier.getUUID());
    }

    /**
     * Shuts down the threads running the Scuttlemutt object.
     */
    public void shutdown() {
        this.meshDaemon.shutdown();
    }
}
