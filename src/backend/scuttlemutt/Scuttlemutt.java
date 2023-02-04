package backend.scuttlemutt;

import java.util.*;

import org.apache.commons.lang3.RandomStringUtils;

import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;
import storagemanager.StorageManager;
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

    public Scuttlemutt(String userContact, IOManager inputIoManager, DawgIdentifier dawgIdentifier, StorageManager storageManager){
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
     * @param recipientUUID Recipient's UUID
     * @return UUID of sent message
     */
    public UUID sendMessage(String message, UUID recipientUUID){
        DawgIdentifier recipientDawgId = this.storageManager.lookupDawgIdentifier(recipientUUID);
        // TODO : Implement seq ID in conversations
        // Placeholder for now
        Long seqId = 0L;
        return this.meshDaemon.sendMessage(message, recipientDawgId, seqId);
    }

    public StorageManager getStorageManager(){
        return this.storageManager;
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
