package backend.iomanager;

import types.BarkPacket;

/**
 * Interface used for classes which hookup the mesh daemon with the OS I/O.
 */
public interface IOManager {
    /**
     * Broadcasts the passed BarkPacket to all nearby devices which are members of the network.
     * @param packet  The packet being broadcasted.
     */
    void broadcast(final BarkPacket packet);

    /**
     * Waits for a BarkPacket to be received by the device, then returns it.
     *
     * @return a BarkPacket received by the device.
     */
    BarkPacket receive();
}
