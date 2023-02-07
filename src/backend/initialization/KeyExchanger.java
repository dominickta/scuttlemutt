package backend.initialization;

import backend.iomanager.IOManager;

/**
 * This class is used to exchange keys between two connected devices.
 *
 * NOTE:  Since the key exchange process is meant to be exclusively P2P, we don't want them to be repropagated over the
 *        mesh network.  As a result, this process is entirely separate from the MeshDaemon.
 */
public class KeyExchanger {
    private final IOManager ioManager;

    /**
     * Constructs the KeyExchanger.
     * @param ioManager  The IOManager used to exchange keys.
     */
    public KeyExchanger(final IOManager ioManager) {
        this.ioManager = ioManager;
    }


}
