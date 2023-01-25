package backend.simulation;

import backend.iomanager.StreamIOManager;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets up and stores StreamIOManager objects to simulate a network.
 *
 * TODO: At the moment, this class interconnects all objects at construction.  In the future, we should modify the code
 *   to allow us to easily make the network less dense.
 */
public class NetworkSimulation {
    // class variables

    // maps labels -> StreamIOManagers associated with the corresponding PipedOutputStream.
    private final Map<String, StreamIOManager> streamIOManagerMap;

    /**
     * Creates a new NetworkSimulation where all devices are fully interconnected.
     *
     * @param deviceLabels  The labels of the devices on the simulated network.
     */
    public NetworkSimulation(final List<String> deviceLabels) {
        // setup the StreamIOManager for each device.
        this.streamIOManagerMap = new HashMap<String, StreamIOManager>();
        for (final String deviceLabel : deviceLabels) {
            // create a StreamIOManager.
            final StreamIOManager ioManager = new StreamIOManager();

            // stash the ioManager in the streamIOManagerMap.
            streamIOManagerMap.put(deviceLabel, ioManager);
        }

        // connect the StreamIOManagers.
        for (int device1 = 0; device1 < deviceLabels.size(); device1++) {
            for (int device2 = device1 + 1; device2 < deviceLabels.size(); device2++) {
                this.connectDevices(deviceLabels.get(device1), deviceLabels.get(device2));
            }
        }
    }

    /**
     * Obtains the StreamIOManager associated with the passed deviceLabel.
     * @param deviceLabel  The label associated with the desired StreamIOManager.
     * @return  The StreamIOManager associated with the passed label.
     */
    public StreamIOManager getStreamIOManager(final String deviceLabel) {
        return this.streamIOManagerMap.get(deviceLabel);
    }

    /**
     * Sets-up a connection between the two specified devices.
     * @param device1  One of the devices in the connection.
     * @param device2  The other the device in the connection.
     */
    public void connectDevices(final String device1, final String device2) {
        // we should not allow a device to connect to itself.
        if (device1.equals(device2)) {
            throw new UnsupportedOperationException("Cannot connect a device to itself!");
        }

        // get the PipedInputStreams
        final Pair<PipedInputStream, PipedOutputStream> d1toD2 = PipedStreamHelper.getPipedStreamPair();
        final Pair<PipedInputStream, PipedOutputStream> d2toD1 = PipedStreamHelper.getPipedStreamPair();

        // remove the PipedInputStreams from the StreamIOManagers
        streamIOManagerMap.get(device1).connect(device2, d2toD1.getLeft(), d1toD2.getRight());
        streamIOManagerMap.get(device2).connect(device1, d1toD2.getLeft(), d2toD1.getRight());
    }

    /**
     * Removes a connection between the two specified devices.
     * @param device1  One of the devices in the connection.
     * @param device2  The other device in the connection.
     */
    public void disconnectDevices(final String device1, final String device2) {
        // remove the PipedInputStreams from the StreamIOManagers
        streamIOManagerMap.get(device1).disconnect(device2);
        streamIOManagerMap.get(device2).disconnect(device1);
    }
}
