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
     * Waits for a Packet to be received by the device, then returns it.
     * Blocks until a packet is received, even if no connections are available.
     *
     * @return A BarkPacket received by the device.
     */
    Packet receive() throws IOManagerException;

    /**
     * @return The list of ids for available connections.
     */
    Set<String> availableConnections() throws IOManagerException;
}