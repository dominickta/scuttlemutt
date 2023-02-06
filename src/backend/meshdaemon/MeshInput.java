package backend.meshdaemon;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import storagemanager.StorageManager;
import types.Bark;
import types.BarkPacket;
import types.Conversation;
import types.DawgIdentifier;

import java.util.Collections;
import java.util.List;
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

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser The DawgIdentifier for the current user.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final DawgIdentifier currentUser) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.currentUser = currentUser;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Check if ioManager is connected so .call function in Iomanager doesn't fail
                BarkPacket barkPacket = ioManager.receive();
                List<Bark> barkList = barkPacket.getPacketBarks();

                for (Bark bark : barkList) {
                    // if we have seen this bark before, ignore it.
                    if (this.storage.lookupBark(bark.getUniqueId()) != null) {
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
            } catch (IOManagerException e) {
                System.err.println("Failed to receive message: " + e);
            }
        }
    }
}
