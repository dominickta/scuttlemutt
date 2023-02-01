package backend.simulation;

import backend.iomanager.IOManagerException;
import backend.iomanager.QueueIOManager;
import types.BarkPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sets up and stores QueueIOManager objects to simulate a network.
 */
public class NetworkSimulation {
    // Maps device ID to QueueIOManager
    private final Map<String, QueueIOManager> queueIOManagerMap;

    /**
     * Creates a new NetworkSimulation where all devices are fully interconnected.
     *
     * @param deviceLabels  The labels of the devices on the simulated network.
     */
    public NetworkSimulation(final List<String> deviceLabels) {
        // setup the QueueIOManager for each device.
        this.queueIOManagerMap = new HashMap<String, QueueIOManager>();
        for (final String deviceLabel : deviceLabels) {
            // create a QueueIOManager.
            final QueueIOManager ioManager = new QueueIOManager();

            // stash the ioManager in the QueueIOManager.
            queueIOManagerMap.put(deviceLabel, ioManager);
        }
    }


    public void connectAll() {
        List<String> deviceLabels = new ArrayList<>();
        deviceLabels.addAll(this.queueIOManagerMap.keySet());
        // connect the QueueIOManagers.
        for (int device1 = 0; device1 < deviceLabels.size(); device1++) {
            try {
                String label1 = deviceLabels.get(device1);
                Set<String> connections = this.queueIOManagerMap.get(label1).availableConnections();

                for (int device2 = device1 + 1; device2 < deviceLabels.size(); device2++) {
                    String label2 = deviceLabels.get(device2);

                    if (!connections.contains(label2)) {
                        this.connectDevices(label1, label2);
                    }
                }
            } catch (IOManagerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Obtains the QueueIOManager associated with the passed deviceLabel.
     * @param deviceLabel  The label associated with the desired QueueIOManager.
     * @return  The QueueIOManager associated with the passed label.
     */
    public QueueIOManager getQueueIOManager(final String deviceLabel) {
        return this.queueIOManagerMap.get(deviceLabel);
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

        // Build queues to connect devices
        final BlockingQueue<BarkPacket> q1to2 = new LinkedBlockingQueue<BarkPacket>();
        final BlockingQueue<BarkPacket> q2to1 = new LinkedBlockingQueue<BarkPacket>();

        queueIOManagerMap.get(device1).connect(device2, q1to2, q2to1);
        queueIOManagerMap.get(device2).connect(device1, q2to1, q1to2);
    }

    /**
     * Removes a connection between the two specified devices.
     * @param device1  One of the devices in the connection.
     * @param device2  The other device in the connection.
     */
    public void disconnectDevices(final String device1, final String device2) {
        queueIOManagerMap.get(device1).disconnect(device2);
        queueIOManagerMap.get(device2).disconnect(device1);
    }
}
