package scuttlemutt;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;

import org.apache.commons.lang3.RandomStringUtils;

import backend.iomanager.StreamIOManager;
import backend.meshdaemon.MeshDaemon;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

/*
 * This class represents 
 */
public class Scuttlemutt {

    // Identifier object of user
    private final DawgIdentifier dawgIdentifier;
    // I/O manager
    private final StreamIOManager ioManager;
    // Database of connected users, previous conversations, and previous messages
    private final StorageManager storageManager;
    // Daemon controlling recieving and broadcasting of messages
    private final MeshDaemon meshDaemon;
    // Long representing number of messages user has sent for ordering purposes
    private Long seqId;

    /*
     * Constructs a new Scuttlemutt object
     */
    public Scuttlemutt(String userContact){
        this.dawgIdentifier = generateDawgIdentifier(userContact);
        this.ioManager = new StreamIOManager();
        this.storageManager = new MapStorageManager();
        this.meshDaemon = new MeshDaemon(ioManager, storageManager, dawgIdentifier);
        this.seqId = 0L;
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
        DawgIdentifier recipientDawgId = this.lookupDawgIdentifier(recipientUUID);
        System.out.println("RECEPUUID: " + recipientDawgId + "FROM " + this.dawgIdentifier.getUniqueId());
        return this.meshDaemon.sendMessage(message, recipientDawgId, seqId);
    }

    public Bark lookupBark(final UUID barkUuid){
        return this.storageManager.lookupBark(barkUuid);
    }

    public DawgIdentifier lookupDawgIdentifier(final UUID dawgUuid){
        return this.storageManager.lookupDawgIdentifier(dawgUuid);
    }

    public Conversation lookupConversation(final List<UUID> userUuidList){
        return this.storageManager.lookupConversation(userUuidList);
    }

    /*
     * 
     * @return an int of the number of connections the device has
     */
    public int numConnections(){
        return this.ioManager.numConnections();
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

    /**
     * Stores the passed PipedInputStream in the StreamIOManager's list of incoming connections.  This is equivalent
     * to creating the connection between the two devices.
     * 
     * NOTE:  This method should solely be used by the Scuttlemutt NetworkSimulation.  To connect StreamIOManagers, please use the
     * methods inside the ScuttlemuttNetworkSimulation.
     *
     * @param inputStreamFromDevice  A PipedInputStream feeding bytes from the other device.
     * @param outputStreamToDevice  A PipedOutputStream to send bytes to the other device.
     * @param dawgIdentifier A DawgIdentifier of the device we are connecting to
     */
    public void connect(final PipedInputStream inputStreamFromDevice, final PipedOutputStream outputStreamToDevice, final DawgIdentifier otherDawgIdentifier) {
        this.ioManager.connect(otherDawgIdentifier.getUserContact(), inputStreamFromDevice, outputStreamToDevice);
        this.storageManager.storeDawgIdentifier(otherDawgIdentifier);
        System.out.println("Added to " + this.dawgIdentifier.getUniqueId() +  " list: " + otherDawgIdentifier.getUniqueId());
        
    }

    /**
     * Removes the passed PipedInputStream from the StreamIOManager's incoming connections.  This is equivalent
     * to cutting the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the Scuttlemutt NetworkSimulation.  To connect StreamIOManagers, please use the
     * methods inside the ScuttlemuttNetworkSimulation.
     *
     * @param dawgIdentifier The device we're disconnecting from.
     */
    public void disconnect(final DawgIdentifier otherDawgIdentifier) {
        this.ioManager.disconnect(otherDawgIdentifier.getUserContact());
        this.storageManager.deleteDawgIdentifier(otherDawgIdentifier.getUniqueId());
    }


}
