package backend.meshdaemon;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.packet.BarkPacket;
import types.Conversation;
import types.DawgIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Monitors the incoming requests from the IOManager.
 */
public class MeshInput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final StorageManager storage;
    private final BlockingQueue<Bark> queue;
    private final DawgIdentifier currentUser;

    private final Set<Bark> seenBarks;

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser  The DawgIdentifier for the current user.
     * @param seenBarks  A Set containing the Barks we have seen before.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final DawgIdentifier currentUser,
            final Set<Bark> seenBarks) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.currentUser = currentUser;
        this.seenBarks = seenBarks;
    }

    @Override
    public void run() {
        while (true) {
            this.handleInput();
        }
    }

    public void handleInput() {
        // TODO: Add BarkPacket verification (e.g. with signing)

        // Check if ioManager is connected so .call function in Iomanager doesn't fail
        BarkPacket barkPacket = ioManager.meshReceive(BarkPacket.class);
        List<Bark> barkList = barkPacket.getPacketBarks();

        for (Bark bark : barkList) {
            // if we have seen this bark before, ignore it.
            if (!this.seenBarks.add(bark)) {
                continue;
            }

            if (this.currentUser.equals(bark.getReceiver())) {
                // this is for us, store for later
                storage.storeBark(bark);

                // update the Conversation object stored in the StorageManager to include the Bark.
                Conversation c = this.storage.lookupConversation(Collections.singletonList(bark.getSender().getUniqueId()));  // TODO:  If we implement group msgs, revise to support groups.
                if (c == null) {
                    // if we've never initiated a conversation with the sender before, create + store a new Conversation.
                    c = new Conversation(Collections.singletonList(bark.getSender()),
                            Collections.singletonList(bark.getUniqueId()));  // TODO:  If we implement group msgs, revise to support groups.
                    this.storage.storeConversation(c);
                } else {
                    // update existing obj
                    c.storeBarkUUID(bark.getUniqueId());
                    this.storage.storeConversation(c);
                }

            } else if (!this.currentUser.equals(bark.getSender())) {
                this.queue.add(bark); // put it on output buffer
            }
        }
    }
}
