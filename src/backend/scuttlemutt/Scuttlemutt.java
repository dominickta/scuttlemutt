package backend.scuttlemutt;

import java.util.*;
import java.util.stream.Collectors;

import backend.iomanager.QueueIOManager;
import org.apache.commons.lang3.RandomStringUtils;

import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.Conversation;
import types.DawgIdentifier;

/*
 * This class contains and organizes the content necessary to run the local device's node on the Scuttlemutt network.
 */
public class Scuttlemutt {

    // Identifier object of user
    private final DawgIdentifier dawgIdentifier;
    // I/O manager
    private final IOManager ioManager;
    // Database of connected users, previous conversations, and previous messages
    private final StorageManager storageManager;
    // Daemon controlling recieving and broadcasting of messages
    private final MeshDaemon meshDaemon;

    /*
     * Constructs a new Scuttlemutt object
     */
    public Scuttlemutt(IOManager inputIoManager, DawgIdentifier dawgIdentifier, StorageManager storageManager){
        this.dawgIdentifier = dawgIdentifier;
        this.ioManager = inputIoManager;
        this.storageManager = storageManager;
        this.meshDaemon = new MeshDaemon(this.ioManager, this.storageManager, this.dawgIdentifier);
    }

    /**
     * Returns user's DawgIdentifier
     * @return deep copy of user's DawgIdentifier so public key can't be modified
     */
    public DawgIdentifier getDawgIdentifier(){
        return new DawgIdentifier(this.dawgIdentifier.getUserContact(), this.dawgIdentifier.getUniqueId(), this.dawgIdentifier.getPublicKey());
    }

    /**
     * Sends a message to recipient based on UUID
     * @param message Message that user is trying to send
     * @param dstDawgId Recipient's DawgIdentifier
     * @return UUID of sent message
     */
    public UUID sendMessage(String message, DawgIdentifier dstDawgId){
        // TODO : Implement seq ID in conversations
        // Placeholder for now
        final Long seqId = 0L;
        return this.meshDaemon.sendMessage(message, dstDawgId, seqId);
    }

    /**
     * Returns a List containing the active conversations for the user.
     * @return a List containing the active conversations for the user.
     */
    public List<Conversation> listAllConversations() {
        return this.storageManager.listAllConversations();
    }

    /**
     * Returns a List<String> containing the messages of the passed Conversation.
     * @param conversation  the Conversation whose messages we're obtaining.
     * @return a List<String> containing the messages of the passed Conversation.
     */
    public List<String> getMessagesForConversation(final Conversation conversation) {
        final List<String> msgs = new ArrayList<String>();
        for (final UUID uuid : conversation.getBarkUUIDList()) {
            // TODO:  Replace this with code which actually looks up the full msg instead of just one Bark.
            //   (if we don't do this though, the demo should still work given a short enough msg)
            final String msg = this.storageManager
                    .lookupBark(uuid)
                    .getContents();

            msgs.add(msg);
        }
        return msgs;
    }

    public void addContact(final DawgIdentifier dawgIdentifier) {
        this.storageManager.storeDawgIdentifier(dawgIdentifier);
    }

    public void removeContact(final DawgIdentifier dawgIdentifier) {
        // verify that the DawgIdentifier is currently stored.
        if (this.storageManager.lookupDawgIdentifier(dawgIdentifier.getUniqueId()) == null) {
            throw new RuntimeException("Attempted to delete a nonexistent DawgIdentifier!  Scuttlemutt instance:  " + this.dawgIdentifier.getUniqueId().toString()
                    + "\tDawgIdentifier ID:  " + dawgIdentifier.getUniqueId().toString());
        }

        this.storageManager.deleteDawgIdentifier(dawgIdentifier.getUniqueId());
    }

    public Conversation getConversation(final List<DawgIdentifier> dawgIdentifiers) {
        // verify that a Conversation for the DawgIdentifiers is currently stored.
        final List<UUID> convoIds = dawgIdentifiers.stream().map(DawgIdentifier::getUniqueId).toList();

        // attempt to lookup the conversation.
        final Conversation c = this.storageManager.lookupConversation(convoIds);

        if (c == null) {
            final String convoIdListString = dawgIdentifiers.stream()
                    .map(id -> id.getUniqueId().toString())
                    .collect(Collectors.joining(", "));
            throw new RuntimeException("Attempted to get a nonexistent Converation!  Scuttlemutt instance:  " + this.dawgIdentifier.getUniqueId().toString()
                    + "\tConversation DawgIdentifier IDs:  " + convoIdListString);
        }

        return c;
    }

    /**
     * Shuts down the threads running the Scuttlemutt object.
     */
    public void shutdown() {
        this.meshDaemon.shutdown();
    }

    /**
     * Generates a DawgIdentifier for user on creation
     * @param userContact User provided identification string
     * @return new DawgIdentifier with generated UUID and public key
     */
    private DawgIdentifier generateDawgIdentifier(String userContact){
        UUID uuid=UUID.randomUUID();   
        //TODO: Replace this with actual public key generation once 
        // encryption method has been decided
        String publickey = RandomStringUtils.randomAlphabetic(15);
        return new DawgIdentifier(userContact, uuid, publickey);
        
    }
}
