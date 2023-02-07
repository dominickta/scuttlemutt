package backend.iomanager;

import types.BarkPacket;

import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * This class represents a Queue based I/O manager.
 *
 * This manager can be used to simulate a network via feeding the queues to different devices.
 */
public class QueueIOManager implements IOManager {
    // Set of all available connections
    private final Set<String> connections;
    // Maps connection ID to input stream
    private final Map<String, BlockingQueue<BarkPacket>> inputQueues;
    // Maps connection ID to output stream
    private final Map<String, BlockingQueue<BarkPacket>> outputQueues;

    public QueueIOManager() {
        this.connections = new HashSet<>();
        this.inputQueues = new HashMap<>();
        this.outputQueues = new HashMap<>();
    }

    @Override
    public void send(final String receiverId, BarkPacket packet) throws IOManagerException {
        if (!this.outputQueues.containsKey(receiverId)) {
            throw new IOManagerException("No available connection to '" + receiverId + "'");
        }

        this.outputQueues.get(receiverId).add(packet);
    }

    @Override
    public BarkPacket receive() throws IOManagerException {
        while (true) {
            // Randomize the checking order to avoid overpolling.
            // Rebuild the list every iteration to remove dead connections.
            List<BlockingQueue<BarkPacket>> inputs = new ArrayList<>(this.inputQueues.values());
            Collections.shuffle(inputs);

            for (BlockingQueue<BarkPacket> input : inputs) {
                if (input.size() > 0) {
                    // Return on the first message received.
                    return input.remove();
                }
            }

            // Wait 100ms before trying again for performance.
            // Could be arbitrarily long before we have any new messages.
            try {
                Thread.sleep(100);
            } catch (InterruptedException _e) {
                // TODO: Graceful handling of an interrupt.
            }
        }
    }

    @Override
    public Set<String> availableConnections() throws IOManagerException {
        return new HashSet<>(this.connections);
    }

    /**
     * Stores the passed BlockingQueues in the QueueIOManager's connection Lists.  This is equivalent
     * to creating the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the NetworkSimulation.  To connect StreamIOManagers, please use the
     * methods inside the NetworkSimulation.
     *
     * @param deviceId  The other device (which we are setting up the connection with).
     * @param inputQueue  A BlockingQueue feeding bytes from the other device.
     * @param outputQueue  A BlockingQueue to send bytes to the other device.
     */
    public void connect(final String deviceId, final BlockingQueue<BarkPacket> inputQueue, final BlockingQueue<BarkPacket> outputQueue) {
        this.connections.add(deviceId);
        this.inputQueues.put(deviceId, inputQueue);
        this.outputQueues.put(deviceId, outputQueue);
    }

    /**
     * Removes all connections with the specified device.  This is equivalent
     * to cutting the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the NetworkSimulation.  To disconnect StreamIOManagers, please use the
     * methods inside the NetworkSimulation.
     *
     * @param deviceId  The device we're disconnecting from.
     */
    public void disconnect(final String deviceId) {
        if (this.connections.contains(deviceId)) {
            this.connections.remove(deviceId);
            this.inputQueues.remove(deviceId);
            this.outputQueues.remove(deviceId);
        }
    }
}