package backend.scuttlemutt;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;
import crypto.Crypto;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

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
        
        // keys must be initialized befor the mesh daemon is constructed.
        this.meshDaemon = new MeshDaemon(this.ioManager, this.storageManager, this.dawgIdentifier);
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
    public SecretKey getSecretKey(final DawgIdentifier otherDeviceId) {
        return this.storageManager.lookupSecretKeyForUUID(otherDeviceId.getUUID());
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
     * Sends a message to recipient based on UUID
     *
     * @param message   Message that user is trying to send
     * @param dstDawgId Recipient's DawgIdentifier
     * @return UUID of sent message
     */
    public UUID sendMessage(String message, DawgIdentifier dstDawgId) {
        // TODO : Implement seq ID in conversations
        final Long seqId = 0L; // placeholder for now
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
     * @return a List<String> containing the messages of the passed Conversation.
     */
    public List<String> getMessagesForConversation(final Conversation conversation) {
        // get the encryption key associated with the Conversation.
        PrivateKey myPrivateKey = getPrivateKey();
        final UUID otherPersonId = conversation.getOtherPerson().getUUID();
        final SecretKey decryptionKey = this.storageManager.lookupSecretKeyForUUID(otherPersonId);
        return getBarksForConversation(conversation).stream().map(b -> b.getContents(myPrivateKey, decryptionKey))
                .collect(Collectors.toList());
    }

    /**
     * Returns a List<Bark> containing the barks of the passed Conversation.
     *
     * @param conversation the Conversation whose barks we're obtaining.
     * @return a List<Bark> containing the barks of the passed Conversation, or
     *         null.
     */
    public List<Bark> getBarksForConversation(final Conversation conversation) {
        final List<Bark> barks = new ArrayList<Bark>();
        for (final UUID uuid : conversation.getBarkUUIDList()) {
            final Bark bark = this.storageManager.lookupBark(uuid);
            if (bark == null) {
                return null;
            }
            barks.add(bark);
        }
        return barks;
    }

    public List<DawgIdentifier> getAllContacts() {
        return this.storageManager.getAllDawgIdentifiers();
    }

    public boolean haveContact(final UUID uuid) {
        return this.storageManager.lookupDawgIdentifier(uuid) != null;
    }

    public void addContact(final DawgIdentifier dawgIdentifier, final PublicKey publicKey, final SecretKey secretKey) {
        this.storageManager.storeDawgIdentifier(dawgIdentifier);
        this.storageManager.storePublicKeyForUUID(dawgIdentifier.getUUID(), publicKey);
        this.storageManager.storeSecretKeyForUUID(dawgIdentifier.getUUID(), secretKey);
    }

    public void removeContact(final DawgIdentifier dawgIdentifier) {
        // verify that the DawgIdentifier is currently stored.
        if (this.storageManager.lookupDawgIdentifier(dawgIdentifier.getUUID()) == null) {
            String msg = "Attempted to delete a nonexistent DawgIdentifier! ";
            throw new RuntimeException(msg + this.dawgIdentifier.toString());
        }

        this.storageManager.deleteDawgIdentifier(dawgIdentifier.getUUID());
    }

    public Conversation getConversation(final DawgIdentifier dawgIdentifier) {
        // attempt to lookup the conversation.
        final Conversation c = this.storageManager.lookupConversation(dawgIdentifier.getUUID());
        if (c == null) {
            return null;
        }
        return c;
    }

    /**
     * Shuts down the threads running the Scuttlemutt object.
     */
    public void shutdown() {
        this.meshDaemon.shutdown();
    }
}
