package backend.iomanager;

import types.packet.Packet;

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
    private final Map<String, BlockingQueue<Packet>> inputQueues;
    // Maps connection ID to output stream
    private final Map<String, BlockingQueue<Packet>> outputQueues;

    public QueueIOManager() {
        this.connections = new HashSet<>();
        this.inputQueues = new HashMap<>();
        this.outputQueues = new HashMap<>();
    }

    @Override
    public void send(final String receiverId, Packet packet) throws IOManagerException {
        if (!this.outputQueues.containsKey(receiverId)) {
            throw new IOManagerException("No available connection to '" + receiverId + "'");
        }

        this.outputQueues.get(receiverId).add(packet);
    }

    @Override
    public <T extends Packet> T meshReceive(final Class<T> desiredPacketClass) {
        while (true) {
            // Randomize the checking order to avoid overpolling.
            // Rebuild the list every iteration to remove dead connections.
            List<BlockingQueue<Packet>> inputs = new ArrayList<>(this.inputQueues.values());
            Collections.shuffle(inputs);

            for (BlockingQueue<Packet> input : inputs) {
                synchronized (input) {  // obtain a lock on the current BlockingQueue (to ensure singleDeviceReceive
                                        // doesn't accidentally take input.)
                    if (input.size() > 0) {
                        // see if a Packet of the desired type is in the BlockingQueue.
                        final Optional<T> foundPacketOptional
                                = this.getPacketTypeFromBlockingQueue(input, desiredPacketClass);

                        // if a Packet of the desired type was found, return it.
                        if (foundPacketOptional.isPresent()) {
                            return foundPacketOptional.get();
                        }
                    }
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
    public <T extends Packet> T singleDeviceReceive(final String senderId, final Class<T> desiredPacketClass) {
        final BlockingQueue<Packet> inputQueue = inputQueues.get(senderId);
        while (true) {

            synchronized (inputQueue) {  // obtain a lock on the current BlockingQueue (to ensure meshDeviceReceive
                                         // doesn't accidentally take input.)
                if (inputQueue.size() > 0) {
                    // see if a Packet of the desired type is in the BlockingQueue.
                    final Optional<T> foundPacketOptional
                            = this.getPacketTypeFromBlockingQueue(inputQueue, desiredPacketClass);

                    // if a Packet of the desired type was found, return it.
                    if (foundPacketOptional.isPresent()) {
                        return foundPacketOptional.get();
                    }
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
     * NOTE:  This method should solely be used by the NetworkSimulation.  To connect QueueIOManagers, please use the
     * methods inside the NetworkSimulation.
     *
     * @param deviceId  The other device (which we are setting up the connection with).
     * @param inputQueue  A BlockingQueue feeding Packets from the other device.
     * @param outputQueue  A BlockingQueue to send Packets to the other device.
     */
    public void connect(final String deviceId, final BlockingQueue<Packet> inputQueue, final BlockingQueue<Packet> outputQueue) {
        this.connections.add(deviceId);
        this.inputQueues.put(deviceId, inputQueue);
        this.outputQueues.put(deviceId, outputQueue);
    }

    /**
     * Removes all connections with the specified device.  This is equivalent
     * to cutting the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the NetworkSimulation.  To disconnect QueueIOManagers, please use the
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

    /**
     * Obtains the first of the desired Packet type from the passed BlockingQueue.
     *
     * If no such packets of the specified type exist in the BlockingQueue, returns Optional.empty().
     *
     * NOTE:  This method _does not_ empty the BlockingQueue--all Packets which are iterated through + not returned are
     * reloaded upon the queue.
     *
     * @param queue the BlockingQueue which we are polling for the Packet.
     * @param desiredPacketClass the desired class of the Packet.
     * @return an Optional<Packet> containing the first packet of the specified type from the BlockingQueue.  If no such
     * packet of the specified type exists in the queue, returns Optional.empty().
     */
    private <T extends Packet> Optional<T> getPacketTypeFromBlockingQueue(final BlockingQueue<Packet> queue,
                                                            final Class<T> desiredPacketClass) {
        // drain the queue's packets to a List.
        final List<Packet> packetList = new ArrayList<Packet>();
        queue.drainTo(packetList);

        // peruse the List for the desired Packet type.
        int packetIdx = 0;
        for (; packetIdx < packetList.size(); packetIdx++) {
            // if the current Packet is of the correct class type, stop the search.
            if (desiredPacketClass.equals(packetList.get(packetIdx).getClass())) {
                break;
            }
        }

        final Optional<T> foundPacketOptional;
        if (packetIdx < packetList.size()) {
            // if we found a packet of the correct type, remove it from the packetList and store it.
            foundPacketOptional = (Optional<T>) Optional.of(packetList.remove(packetIdx));
        } else {
            // otherwise, store that no packet was found.
            foundPacketOptional = Optional.empty();
        }

        // reload all irrelevant Packets onto the queue.
        queue.addAll(packetList);

        // return the foundPacketOptional.
        return foundPacketOptional;
    }
}
