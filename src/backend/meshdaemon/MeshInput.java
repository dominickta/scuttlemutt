package backend.meshdaemon;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.BarkPacket;
import types.MuttIdentifier;

/**
 * Monitors the incoming requests from the IOManager.
 */
public class MeshInput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final StorageManager storage;
    private final BlockingQueue<Bark> queue;
    private final MuttIdentifier currentUser;

    /**
     * Constructs a new MeshInput.
     * 
     * @param ioManager   The underlying IOManager.
     * @param queue       The queue of outgoing barks to forward
     * @param storage     A StorageManager to store Barks addressed to us.
     * @param currentUser The MuttIdentifier for the current user.
     */
    public MeshInput(final IOManager ioManager, final BlockingQueue<Bark> queue,
            final StorageManager storage, final MuttIdentifier currentUser) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.storage = storage;
        this.currentUser = currentUser;
    }

    @Override
    public void run() {
        while (true) {
            BarkPacket barkPacket = ioManager.receive();
            List<Bark> barkList = barkPacket.getPacketBarks();
            for (Bark bark : barkList) {
                if (bark.getReceiver().equals(this.currentUser)) {
                    storage.storeBark(bark); // this is for us, store for later
                } else {
                    this.queue.add(bark); // put it on output buffer
                }
            }
        }
    }
}
