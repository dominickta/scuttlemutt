package backend.meshdaemon;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import javax.crypto.SecretKey;

/**
 * Controls input/output logic and an internal Bark queue.
 */
public class MeshDaemon {
    private final BlockingQueue<Bark> queue;
    private final MeshInput input;
    private final MeshOutput output;
    private final DawgIdentifier currentUser;
    private final StorageManager storageManager;
    private final Thread inputThread, outputThread;

    /**
     * Constructs a new MeshDaemon.
     * 
     * @param ioManager The underlying IOManager.
     * @param storageManager   The place to store messages + conversations meant for us.
     */
    public MeshDaemon(final IOManager ioManager, final StorageManager storageManager, final DawgIdentifier currentUser) {
        // Shared state between input and output
        Set<Bark> seenBarks = new HashSet<>();

        this.currentUser = currentUser;
        this.queue = new LinkedBlockingQueue<>();
        this.input = new MeshInput(ioManager, queue, storageManager, currentUser, seenBarks);
        this.output = new MeshOutput(ioManager, queue, seenBarks);
        this.storageManager = storageManager;

        // Spin out two threads, one to block on the IOManager's receive() and
        // the other to spin on the queue, and passing them to the IOMonitor's
        // broadcast().
        this.inputThread = new Thread(input);
        this.outputThread = new Thread(output);
        this.inputThread.start();
        this.outputThread.start();
    }

    /**
     * Shuts down the threads which run the MeshDaemon.
     */
    public void shutdown() {
        this.inputThread.interrupt();
        this.outputThread.interrupt();
    }

    /**
     * Adds the given message to the outbound queue.
     * 
     * @param contents  The message contents.
     * @param recipient The DawgIdentifier of who is receiving the message.
     * @param seqId     The sequence number for this message.
     * @returns UUID of sent bark
     */
    public UUID sendMessage(String contents, DawgIdentifier recipient, Long seqId) {
        final SecretKey encryptionKey = this.storageManager.lookupKeyForDawgIdentifier(recipient.getUniqueId());
        final Bark barkMessage = new Bark(contents, this.currentUser, recipient, seqId, encryptionKey);

        // update the Conversation object stored in the StorageManager to include the Bark.
        Conversation c = this.storageManager.lookupConversation(Collections.singletonList(recipient.getUniqueId()));  // TODO:  If we implement group msgs, revise to support groups.
        if (c == null) {
            // if we've never initiated a conversation with the sender before, create + store a new Conversation.
            c = new Conversation(Collections.singletonList(recipient),
                    Collections.singletonList(barkMessage.getUniqueId()));  // TODO:  If we implement group msgs, revise to support groups.
            this.storageManager.storeConversation(c);
        } else {
            // update existing obj
            c.storeBarkUUID(barkMessage.getUniqueId());
            this.storageManager.storeConversation(c);
        }

        // store the Bark in the database.
        this.storageManager.storeBark(barkMessage);

        queue.add(barkMessage);
        return barkMessage.getUniqueId();
    }
}
