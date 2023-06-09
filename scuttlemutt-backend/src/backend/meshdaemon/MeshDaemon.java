package backend.meshdaemon;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;

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
     * @param ioManager      The underlying IOManager.
     * @param storageManager The place to store messages + conversations meant for
     *                       us.
     */
    public MeshDaemon(final IOManager ioManager, final StorageManager storageManager,
                      final DawgIdentifier currentUser) {
        // Shared state between input and output
        Set<Bark> seenBarks = Collections.synchronizedSet(new HashSet<>());

        // grab the private key
        PrivateKey privateKey = storageManager.lookupPrivateKey();

        this.currentUser = currentUser;
        this.queue = new LinkedBlockingQueue<>();
        this.input = new MeshInput(ioManager, queue, storageManager, privateKey, seenBarks);
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
     * @return UUID of sent bark
     */
    public UUID sendMessage(String contents, DawgIdentifier recipient, Long seqId) {
        UUID recipientId = recipient.getUUID();
        final SecretKey recipientSecretKey = this.storageManager.lookupLatestSecretKeyForUuid(recipientId);
        final PublicKey recipientPublicKey = this.storageManager.lookupPublicKeyForUUID(recipientId);
        final PrivateKey senderPrivateKey = this.storageManager.lookupPrivateKey();
        final Bark barkMessage = new Bark(contents, this.currentUser,
                seqId, senderPrivateKey, recipientPublicKey, recipientSecretKey);

        // create a plaintext object to represent the Message.
        final Message message = new Message(contents, seqId, this.currentUser);

        // update the Conversation object stored in the StorageManager to include Bark.
        Conversation c = this.storageManager.lookupConversation(recipientId);
        if (c == null) {
            // if we've never initiated a conversation with the sender before, create +
            c = new Conversation(recipient, Collections.singletonList(message.getUniqueId()));
        } else {
            // update existing obj
            c.storeMessageUUID(message.getUniqueId());
        }
        // store a new Conversation.
        this.storageManager.storeConversation(c);

        // store the plaintext Message object in the database.
        this.storageManager.storeMessage(message);

        // store the Bark in the database.
        this.storageManager.storeBark(barkMessage);

        this.queue.add(barkMessage);
        return barkMessage.getUniqueId();
    }
}
