package backend.meshdaemon;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import javax.crypto.SecretKey;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.packet.BarkPacket;

/**
 * Monitors the incoming requests from the IOManager.
 */
public class MeshInput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final StorageManager storage;
    private final BlockingQueue<Bark> queue;
    private final PrivateKey myPrivateKey;
    private final SecretKey encryptionKey;
    private final Set<Bark> seenBarks;

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser The DawgIdentifier for the current user.
     * @param seenBarks   A Set containing the Barks we have seen before.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final PrivateKey myPrivateKey,
            final SecretKey encryptionKey, final Set<Bark> seenBarks) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.myPrivateKey = myPrivateKey;
        this.encryptionKey = encryptionKey;
        this.seenBarks = seenBarks;
    }

    @Override
    public void run() {
        while (true) {
            this.handleInput();
        }
    }

    public void handleInput() {
        // TODO: Add BarkPacket verification (e.g. crypto signatures)

        // Check if ioManager is connected so .call function in Iomanager doesn't fail
        BarkPacket barkPacket = ioManager.meshReceive(BarkPacket.class);
        List<Bark> barkList = barkPacket.getPacketBarks();

        for (Bark bark : barkList) {
            // if we have seen this bark before, ignore it.
            if (!this.seenBarks.add(bark)) {
                continue;
            }

            if (bark.isForMe(this.myPrivateKey)) {
                // this is for us, store for later
                storage.storeBark(bark);

                // update Conversation object stored in the StorageManager to include the Bark.
                DawgIdentifier sender = bark.getSender(this.myPrivateKey, this.encryptionKey);
                Conversation c = this.storage.lookupConversation(sender.getUUID());
                if (c == null) {
                    // if we've never initiated a conversation with the sender before, create +
                    // store a new Conversation.
                    c = new Conversation(sender, Collections.singletonList(bark.getUniqueId()));
                    this.storage.storeConversation(c);
                } else {
                    // update existing obj
                    c.storeBarkUUID(bark.getUniqueId());
                    this.storage.storeConversation(c);
                }

            } else {
                this.queue.add(bark); // put it on output buffer
            }
        }
    }
}
