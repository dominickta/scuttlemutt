package backend.meshdaemon;

import backend.iomanager.IOManager;
import backend.iomanager.IOManagerException;
import types.Bark;
import types.packet.BarkPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Controls outbound messages from the mesh daemon.
 *
 * This class is runnable and should be run in a separate thread since it might
 * block (it will probably block).
 */
public class MeshOutput implements Runnable {
    // Number of unique devices that each packet must be broadcasted to before being dropped.
    public static final int NUM_REBROADCAST_BEFORE_DROP = 2;

    private static final long RETRY_SLEEP_MILLIS = 100;

    private final IOManager ioManager;
    private final BlockingQueue<Bark> queue;

    private BarkPacket currentBarkPacket;
    private Set<String> successfulSends;
    private Set<Bark> seenBarks;

    /**
     * Constructs a new MeshOutput.
     *
     * @param ioManager The underlying IOManager.
     * @param queue     A queue of barks to send out.
     */
    public MeshOutput(final IOManager ioManager, final BlockingQueue<Bark> queue,
                      Set<Bark> seenBarks) {
        this.ioManager = ioManager;
        this.queue = queue;
        this.currentBarkPacket = null;
        this.seenBarks = seenBarks;
        this.successfulSends = new HashSet<>();
    }

    @Override
    public void run() {
        while (true) {
            this.handleOutput();
        }
    }

    public void handleOutput() {
        // TODO list:
        // - Create BarkPackets tailored for each receiver.
        // - Keep track of who successfully got a packet.
        // - Verify valid receivers from the DatabaseManager.
        // - Maintain/use a list of receivers that are blocked (e.g. for spam).
        // - Sign/encrypt messages before sending out.
        if (this.currentBarkPacket == null) {
            try {
                Bark nextBark = this.queue.take();
                this.seenBarks.add(nextBark);
                this.currentBarkPacket = new BarkPacket(List.of(nextBark));
            } catch (InterruptedException _e) {
                // TODO: Add logging/cleanup as necessary.
                return;
            }
        }

        Set<String> receiverIds;
        try {
            receiverIds = this.ioManager.availableConnections();
        } catch (IOManagerException e) {
            System.err.println("Failed to get available connections -- " + e);
            return;
        }

        if (receiverIds.size() > 0) {
            // Don't resend to previously sent devices.
            receiverIds.removeAll(this.successfulSends);

            for (String receiverId : receiverIds) {
                try {
                    this.ioManager.send(receiverId, new BarkPacket(this.currentBarkPacket));
                    successfulSends.add(receiverId);
                } catch (IOManagerException e) {
                    System.err.println("Failed to send to '" + receiverId + "' -- " + e);
                }
            }

            // Only drop the current packet if we reached the send threshold.
            if (successfulSends.size() >= NUM_REBROADCAST_BEFORE_DROP) {
                this.currentBarkPacket = null;
                this.successfulSends.clear();
            }
        } else {
            // Wait before trying again for performance.
            // Could be arbitrarily long before we have any connections.
            try {
                Thread.sleep(RETRY_SLEEP_MILLIS);
            } catch (InterruptedException _e) {
                // TODO: Add logging/cleanup as necessary.
                return;
            }
        }
    }
}
