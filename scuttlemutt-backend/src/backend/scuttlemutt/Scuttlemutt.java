package backend.scuttlemutt;

import java.util.*;
import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import static java.util.stream.Collectors.toList;

import javax.crypto.SecretKey;


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
        this.meshDaemon = new MeshDaemon(this.ioManager, this.storageManager, this.dawgIdentifier);
    }

    /**
     * Returns user's DawgIdentifier
     *
     * @return deep copy of user's DawgIdentifier so public key can't be modified
     */
    public DawgIdentifier getDawgIdentifier(){
        return new DawgIdentifier(this.dawgIdentifier.getUserContact(), this.dawgIdentifier.getUniqueId());
    }

    /**
     * Returns the SecretKey used to chat with the device associated with the passed DawgIdentifier.
     *
     * @param otherDeviceId  A DawgIdentifier used to identify the other device.
     * @return the SecretKey used to chat with the device associated with the passed DawgIdentifier.
     */
    public SecretKey getKey(final DawgIdentifier otherDeviceId){
        return this.storageManager.lookupKeyForDawgIdentifier(otherDeviceId.getUniqueId());
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
        // Placeholder for now
        final Long seqId = 0L;
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
        final SecretKey decryptionKey = this.storageManager.lookupKeyForDawgIdentifier(conversation.getUserList().get(0).getUniqueId());

        return getBarksForConversation(conversation).stream().map(b -> b.getContents(decryptionKey)).collect(toList());
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

    public boolean haveContact(final UUID muttNetworkUUID) {
        return this.storageManager.lookupDawgIdentifier(muttNetworkUUID) != null;
    }

    public void addContact(final DawgIdentifier dawgIdentifier, final SecretKey secretKey) {
        this.storageManager.storeDawgIdentifier(dawgIdentifier);
        this.storageManager.storeKeyForDawgIdentifier(dawgIdentifier.getUniqueId(), secretKey);
    }

    public void removeContact(final DawgIdentifier dawgIdentifier) {
        // verify that the DawgIdentifier is currently stored.
        if (this.storageManager.lookupDawgIdentifier(dawgIdentifier.getUniqueId()) == null) {
            throw new RuntimeException("Attempted to delete a nonexistent DawgIdentifier!  Scuttlemutt instance:  "
                    + this.dawgIdentifier.getUniqueId().toString()
                    + "\tDawgIdentifier ID:  " + dawgIdentifier.getUniqueId().toString());
        }

        this.storageManager.deleteDawgIdentifier(dawgIdentifier.getUniqueId());
    }

    public Conversation getConversation(final List<DawgIdentifier> dawgIdentifiers) {
        // verify that a Conversation for the DawgIdentifiers is currently stored.
        final List<UUID> convoIds = dawgIdentifiers.stream().map(DawgIdentifier::getUniqueId).collect(toList());

        // attempt to lookup the conversation.
        final Conversation c = this.storageManager.lookupConversation(convoIds);

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
