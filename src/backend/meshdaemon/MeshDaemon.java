package backend.meshdaemon;

import backend.iomanager.IOManager;
import storagemanager.StorageManager;
import types.Bark;
import types.DawgIdentifier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Controls input/output logic and an internal Bark queue.
 */
public class MeshDaemon {
    // class variables
    private final BlockingQueue<Bark> queue;
    private final MeshInput input;
    private final MeshOutput output;
    private final DawgIdentifier currentUser;

    /**
     * Constructs a new MeshDaemon.
     * 
     * @param ioManager The underlying IOManager.
     * @param storage   The place to store messages meant for us.
     */
    public MeshDaemon(final IOManager ioManager, final StorageManager storage, final DawgIdentifier currentUser) {
        this.currentUser = currentUser;
        this.queue = new LinkedBlockingQueue<>();
        this.input = new MeshInput(ioManager, queue, storage, currentUser);
        this.output = new MeshOutput(ioManager, queue);

        // Spin out two threads, one to block on the IOMonitor's recieve() and
        // the other to spin on the queue, and passing them to the IOMonitor's
        // broadcast().
        new Thread(input).start();
        new Thread(output).start();
    }

    /**
     * Adds the given message to the outbound queue.
     * 
     * @param contents  The message contents.
     * @param recipient The DawgIdentifier of who is receiving the message.
     * @param seqId     The sequence number for this message.
     */
    public void sendMessage(String contents, DawgIdentifier recipient, Long seqId) {
        queue.add(new Bark(contents, this.currentUser, recipient, seqId));
    }
}
