package backend.meshdaemon;

import backend.iomanager.IOManager;
import backend.iomanager.StreamIOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.BarkPacket;
import types.DawgIdentifier;

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
            // Check if ioManager is connected so .call function in Iomanager doesn't fail
            System.out.println(ioManager.numConnections());
            if(ioManager.numConnections() > 1){
                System.err.print("WAITING FOR MESSAGE " + this.currentUser.getUniqueId());
                BarkPacket barkPacket = ioManager.receive();
                System.err.print("GOT MESSAGE");
                List<Bark> barkList = barkPacket.getPacketBarks();
                for (Bark bark : barkList) {
                    System.out.println("RECIEVED MESSAGE " + this.currentUser.getUniqueId());
                    if (bark.getReceiver().equals(this.currentUser)) {
                        storage.storeBark(bark); // this is for us, store for later
                    } else {
                        System.err.println("NOT FOR ME");
                        this.queue.add(bark); // put it on output buffer
                    }
                }
            }
        }
    }
}
