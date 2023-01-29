package backend.simulation;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import backend.iomanager.StreamIOManager;
import scuttlemutt.Scuttlemutt;
import types.DawgIdentifier;

/**
 * Sets up and stores Scuttlemutt objects to simulate a network.
 * Based off of NetworkSimulation class
 **/
public class ScuttlemuttNetworkSimulation {
        // class variables

    // maps labels -> StreamIOManagers associated with the corresponding PipedOutputStream.
    private final Map<String, Scuttlemutt> scuttlemuttManagerMap;

    /**
     * Creates a new ScuttlemuttNetworkSimulation where all devices are fully interconnected.
     *
     * @param userContacts  The userids of the devices on the simulated network.
     */
    public ScuttlemuttNetworkSimulation(final List<String> userContacts) {
        // setup the StreamIOManager for each device.
        this.scuttlemuttManagerMap = new HashMap<String, Scuttlemutt>();
        for (final String userContact : userContacts) {
            final Scuttlemutt scuttlemutt = new Scuttlemutt(userContact);

            // stash the ioManager in the streamIOManagerMap.
            scuttlemuttManagerMap.put(userContact, scuttlemutt);
        }
        // connect the StreamIOManagers.
        for (int device1 = 0; device1 < userContacts.size(); device1++) {
            for (int device2 = device1 + 1; device2 < userContacts.size(); device2++) {
                this.connectDevices(scuttlemuttManagerMap.get(userContacts.get(device1)).getDawgIdentifier(), scuttlemuttManagerMap.get(userContacts.get(device2)).getDawgIdentifier());
            }
        }
    }

    /**
     * Obtains the Scuttlemutt associated with the passed userContact.
     * @param userContact  The label associated with the desired Scuttlemutt.
     * @return  The Scuttlemutt associated with the passed label.
     */
    public Scuttlemutt getScuttlemutt(final String userContact) {
        return this.scuttlemuttManagerMap.get(userContact);
    }

    /**
     * Sets-up a connection between the two specified devices.
     * @param device1  The DawgIdentifier of one of the devices in the connection.
     * @param device2  The DawgIdentifier of the other device in the connection.
     */
    public void connectDevices(final DawgIdentifier device1, final DawgIdentifier device2) {
        // we should not allow a device to connect to itself.
        if (device1.equals(device2)) {
            throw new UnsupportedOperationException("Cannot connect a device to itself!");
        }

        // get the PipedInputStreams
        final Pair<PipedInputStream, PipedOutputStream> d1toD2 = PipedStreamHelper.getPipedStreamPair();
        final Pair<PipedInputStream, PipedOutputStream> d2toD1 = PipedStreamHelper.getPipedStreamPair();

        // remove the PipedInputStreams from the StreamIOManagers
        scuttlemuttManagerMap.get(device1.getUserContact()).connect(d2toD1.getLeft(), d1toD2.getRight(), device2);
        scuttlemuttManagerMap.get(device2.getUserContact()).connect(d1toD2.getLeft(), d2toD1.getRight(), device1);
    }

    /**
     * Removes a connection between the two specified devices.
     * @param device1  One of the devices in the connection.
     * @param device2  The other device in the connection.
     */
    public void disconnectDevices(final DawgIdentifier device1, final DawgIdentifier device2) {
        // remove the PipedInputStreams from the StreamIOManagers
        scuttlemuttManagerMap.get(device1).disconnect(device2);
        scuttlemuttManagerMap.get(device2).disconnect(device1);
    }
}
