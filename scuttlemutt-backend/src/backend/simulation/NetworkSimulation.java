package backend.simulation;

import backend.scuttlemutt.Scuttlemutt;
import backend.iomanager.IOManagerException;
import backend.iomanager.QueueIOManager;
import crypto.Crypto;
import org.apache.commons.lang3.RandomStringUtils;
import storagemanager.MapStorageManager;
import types.DawgIdentifier;
import types.packet.Packet;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Sets up and stores QueueIOManager objects to simulate a network.
 */
public class NetworkSimulation {
    // Maps device ID to QueueIOManager
    private final Map<String, QueueIOManager> queueIOManagerMap;
    // maps labels -> ScuttleMutt associated with the corresponding label.
    private final Map<String, Scuttlemutt> scuttlemuttMap;

    /**
     * Creates a new NetworkSimulation where all devices are fully interconnected.
     *
     * @param deviceLabels  The labels of the devices on the simulated network.
     */
    public NetworkSimulation(final List<String> deviceLabels) {
        // setup the QueueIOManager + Scuttlemutt for each device.
        this.queueIOManagerMap = new HashMap<String, QueueIOManager>();
        this.scuttlemuttMap = new HashMap<String, Scuttlemutt>();
        for (final String deviceLabel : deviceLabels) {
            // create a QueueIOManager.
            final QueueIOManager ioManager = new QueueIOManager();

            // stash the ioManager in the QueueIOManager.
            queueIOManagerMap.put(deviceLabel, ioManager);

            // create a Scuttlemutt object which references the above QueueIOManagers
            // TODO:  Replace with crypto functionality in the future.
            final DawgIdentifier dawgId = new DawgIdentifier(deviceLabel, UUID.randomUUID(), Crypto.generateKeyPair().getPublic());
            final Scuttlemutt scuttlemutt = new Scuttlemutt(ioManager, dawgId, new MapStorageManager());

            // stash the ioManager in the scuttlemuttMap.
            scuttlemuttMap.put(deviceLabel, scuttlemutt);
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
     * Shuts down all the Threads + async processes running the NetworkSimulation.
     */
    public void shutdown() {
        for (final Scuttlemutt s : this.scuttlemuttMap.values()) {
            s.shutdown();
        }
    }

    /**
     * Obtains the Scuttlemutt object associated with the passed deviceLabel.
     * @param deviceLabel  The label associated with the desired Scuttlemutt object.
     * @return  The Scuttlemutt object associated with the passed label.
     */
    public Scuttlemutt getScuttlemutt(final String deviceLabel) {
        if (!this.scuttlemuttMap.containsKey(deviceLabel)) {
            throw new RuntimeException("Device not found!  Device:  " + deviceLabel);
        }
        return this.scuttlemuttMap.get(deviceLabel);
    }

    /**
     * Obtains the QueueIOManager associated with the passed deviceLabel.
     * @param deviceLabel  The label associated with the desired QueueIOManager.
     * @return  The QueueIOManager associated with the passed label.
     */
    public QueueIOManager getQueueIOManager(final String deviceLabel) throws IOManagerException {
        if (!this.queueIOManagerMap.containsKey(deviceLabel)) {
            throw new IOManagerException("Device not found!\tLabel:  " + deviceLabel);
        }
        return this.queueIOManagerMap.get(deviceLabel);
    }

    /**
     * Sets-up a connection between the two specified devices.
     * @param device1Label  The label of one of the devices in the connection.
     * @param device2Label  The label of the other device in the connection.
     */
    public void connectDevices(final String device1Label, final String device2Label) {
        // we should not allow a device to connect to itself.
        if (device1Label.equals(device2Label)) {
            throw new UnsupportedOperationException("Cannot connect a device to itself!");
        }

        // save the contacts in both devices.
        final Scuttlemutt device1 = scuttlemuttMap.get(device1Label);
        final Scuttlemutt device2 = scuttlemuttMap.get(device2Label);
        device1.addContact(device2.getDawgIdentifier());
        device2.addContact(device1.getDawgIdentifier());

        // Build queues to connect devices
        final BlockingQueue<Packet> q1to2 = new LinkedBlockingQueue<Packet>();
        final BlockingQueue<Packet> q2to1 = new LinkedBlockingQueue<Packet>();

        // add the queues to the QueueIOManagers
        queueIOManagerMap.get(device1Label).connect(device2Label, q1to2, q2to1);
        queueIOManagerMap.get(device2Label).connect(device1Label, q2to1, q1to2);
    }

    /**
     * Removes a connection between the two specified devices.I
     * @param device1Label  The label of one of the devices in the connection.
     * @param device2Label  The label of the other device in the connection.
     */
    public void disconnectDevices(final String device1Label, final String device2Label) {

        // delete the contacts from both devices.
        final Scuttlemutt device1 = scuttlemuttMap.get(device1Label);
        final Scuttlemutt device2 = scuttlemuttMap.get(device2Label);
        device1.removeContact(device2.getDawgIdentifier());
        device2.removeContact(device1.getDawgIdentifier());

        // remove the queues from the QueueIOManagers
        queueIOManagerMap.get(device1Label).disconnect(device2Label);
        queueIOManagerMap.get(device2Label).disconnect(device1Label);
    }
}
