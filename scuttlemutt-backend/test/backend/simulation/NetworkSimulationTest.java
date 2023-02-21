package backend.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.powermock.reflect.Whitebox;

import backend.iomanager.IOManagerException;
import backend.iomanager.QueueIOManager;
import backend.scuttlemutt.Scuttlemutt;
import storagemanager.StorageManager;
import types.Bark;
import types.Conversation;
import types.DawgIdentifier;
import types.Message;
import types.TestUtils;
import types.packet.BarkPacket;

public class NetworkSimulationTest {
    // test constants
    public static final int NUM_DEVICES = 3; // NOTE: Value must be >= 2 for tests to run successfully.

    // test variables
    private List<String> deviceLabels;
    private NetworkSimulation simulation;
    private BarkPacket barkPacket;

    @BeforeEach
    public void setup() {
        // generate the device labels.
        deviceLabels = Stream.generate(() -> RandomStringUtils.randomAlphanumeric(15))
                .limit(NUM_DEVICES)
                .collect(Collectors.toList());

        // setup a BarkPacket we can use for testing.
        final Bark bark = TestUtils.generateRandomizedBark();
        barkPacket = new BarkPacket(Collections.singletonList(bark));

        // initialize the simulation.
        simulation = new NetworkSimulation(deviceLabels);

        simulation.connectAll();
    }

    @Test
    public void testGetQueueIOManager_sendMessage_verifyAllDevicesReceivedMessage() {
        try {
            // get the sender QueueIOManager.
            final QueueIOManager sender = simulation.getQueueIOManager(deviceLabels.get(0));

            for (int i = 1; i < deviceLabels.size(); i++) {
                // send the message.
                sender.send(deviceLabels.get(i), barkPacket);
            }

            // verify that all other QueueIOManagers successfully received the message.
            for (int i = 1; i < deviceLabels.size(); i++) {
                final QueueIOManager receiver = simulation.getQueueIOManager(deviceLabels.get(i));
                assertEquals(barkPacket, receiver.meshReceive(BarkPacket.class));
            }

        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    @Test
    @Timeout(value = 10)
    public void testScuttlemutt_sendMessage_verifyDestinationScuttlemuttObjectRecievedMessage() {
        // get the Scuttlemutt for the devices.
        final String aliceLabel = deviceLabels.get(0);
        final String bobLabel = deviceLabels.get(1);
        final Scuttlemutt alice = simulation.getScuttlemutt(aliceLabel);
        final Scuttlemutt bob = simulation.getScuttlemutt(bobLabel);
        final DawgIdentifier aliceId = alice.getDawgIdentifier();
        final DawgIdentifier bobId = bob.getDawgIdentifier();
        final StorageManager aliceStorage = simulation.getStorageManager(aliceLabel);
        final StorageManager bobStorage = simulation.getStorageManager(bobLabel);

        // create a message to send to a specific party.
        // NOTE: The message should be small enough to fit in a single Bark object.
        final String msg = RandomStringUtils.randomAlphanumeric(15);
        final List<SecretKey> keys = bobStorage.lookupSecretKeysForUUID(aliceId.getUUID());
        assertTrue(keys.size() == 1);

        // verify that the keys are correct
        final PublicKey bobPubKey = aliceStorage.lookupPublicKeyForUUID(bobId.getUUID());
        final PublicKey alicePubKey = bobStorage.lookupPublicKeyForUUID(aliceId.getUUID());
        assertEquals(bobPubKey, bob.getPublicKey());
        assertEquals(alicePubKey, alice.getPublicKey());

        // send the message using `sendMessage`
        alice.sendMessage(msg, bobId);

        // give the message a couple seconds to propagate thru the network.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // verify that the intended destination device successfully received the
        // message.
        Conversation c = bob.getConversation(aliceId);
        final List<Message> receivedMsgs = bob.getMessagesForConversation(c);
        assertEquals(1, receivedMsgs.size());
        assertEquals(msg, receivedMsgs.get(0).getPlaintextMessage());
    }

    @Test
    public void testDisconnect_verifyDisconnect_thenReconnect_verifyReconnect() {
        try {
            // get the QueueIOManagers for the devices.
            final String device1Label = deviceLabels.get(0);
            final String device2Label = deviceLabels.get(1);
            final QueueIOManager device1 = simulation.getQueueIOManager(device1Label);
            final QueueIOManager device2 = simulation.getQueueIOManager(device2Label);

            // disconnect device1 and device2.
            simulation.disconnectDevices(device1Label, device2Label);

            // peek inside device1 and device2 and verify that they are disconnected
            // (the number of input streams is < the number of devices on the network).
            Set<String> connections1 = Whitebox.getInternalState(device1, "connections");
            Set<String> connections2 = Whitebox.getInternalState(device2, "connections");
            assertEquals(NUM_DEVICES - 2, connections1.size()); // there should be <NUM_DEVICES - self - disconnected
                                                                // devices> connections.
            assertEquals(NUM_DEVICES - 2, connections2.size());

            // reconnect device1 and device2.
            simulation.connectDevices(device1Label, device2Label);

            // peek inside device1 and device2 and verify that they are disconnected
            // (the number of input streams is < the number of devices on the network).
            connections1 = Whitebox.getInternalState(device1, "connections");
            connections2 = Whitebox.getInternalState(device2, "connections");
            assertEquals(NUM_DEVICES - 1, connections1.size()); // there should be <NUM_DEVICES - self> connections.
            assertEquals(NUM_DEVICES - 1, connections2.size());

            // assert that we can send messages between device1 and device2
            device1.send(device2Label, barkPacket);
            device2.send(device1Label, barkPacket);
            final BarkPacket receivedPacket1 = device1.meshReceive(BarkPacket.class);
            final BarkPacket receivedPacket2 = device2.meshReceive(BarkPacket.class);
            assertEquals(this.barkPacket, receivedPacket1);
            assertEquals(this.barkPacket, receivedPacket2);

        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConnect_connectDeviceToItself_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> simulation.connectDevices(deviceLabels.get(0), deviceLabels.get(0)));
    }

    @Test
    public void testGetQueueIOManager_invalidManager_throwsRuntimeException() {
        final String invalidDevice = "ThisIsAnInvalidDevice";
        assertThrows(IOManagerException.class,
                () -> simulation.getQueueIOManager(invalidDevice));
    }
}
