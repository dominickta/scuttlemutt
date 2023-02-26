package backend.iomanager;

import java.util.Set;

import types.packet.Packet;

/**
 * Interface used for classes which hookup the mesh daemon with the OS I/O.
 */
public interface IOManager {
    /**
     * Broadcasts the passed Packet to all nearby devices which are members of the network.
     * @param receiverId The id of the receiver to send to.
     * @param packet     The packet being broadcasted.
     * @throws IOManagerException On any failure to send.
     */
    void send(final String receiverId, final Packet packet) throws IOManagerException;

    /**
     * Waits for a Packet to be received by the device from the mesh network, then returns it.
     * Blocks until a packet is received, even if no connections are available.
     *
     * @param desiredPacketClass the class of the Packet type we wish to receive.
     * @return A Packet received by the device.
     */
    <T extends Packet> T meshReceive(final Class<T> desiredPacketClass);

    /**
     * Waits for a Packet to be received by the device from the specified device, then returns it.
     * Blocks until a packet is received.
     *
     * @param senderId  the ID of the packet's sender.
     * @param desiredPacketClass the class of the Packet type we wish to receive.
     * @return a Packet from the specified sender.
     */
    <T extends Packet> T singleDeviceReceive(final String senderId, final Class<T> desiredPacketClass);

    /**
     * @return The list of ids for available connections.
     */
    Set<String> availableConnections() throws IOManagerException;

}