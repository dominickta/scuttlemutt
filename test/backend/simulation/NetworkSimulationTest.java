package backend.simulation;

import backend.iomanager.StreamIOManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;
import types.BarkPacket;

import java.io.PipedInputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkSimulationTest {
    // test constants
    public static final int NUM_DEVICES = 3;  // NOTE:  Value must be >= 2 for tests to run successfully.

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
        final String message = RandomStringUtils.randomAlphanumeric(15);
        barkPacket = new BarkPacket(message.getBytes());

        // initialize the simulation.
        simulation = new NetworkSimulation(deviceLabels);
    }

    @Test
    public void testGetStreamIOManager_sendMessage_verifyAllDevicesReceivedMessage() {
        // get the sender StreamIOManager.
        final StreamIOManager sender = simulation.getStreamIOManager(deviceLabels.get(0));

        // send the message.
        sender.broadcast(barkPacket);

        // verify that all other StreamIOManagers successfully received the message.
        for (int i = 1; i < deviceLabels.size(); i++) {
            final StreamIOManager receiver = simulation.getStreamIOManager(deviceLabels.get(i));
            assertEquals(barkPacket, receiver.receive());
        }
    }

    @Test
    public void testDisconnect_verifyDisconnect_thenReconnect_verifyReconnect() {
        // get the StreamIOManagers for the devices.
        final String device1Label = deviceLabels.get(0);
        final String device2Label = deviceLabels.get(1);
        final StreamIOManager device1 = simulation.getStreamIOManager(device1Label);
        final StreamIOManager device2 = simulation.getStreamIOManager(device2Label);

        // disconnect device1 and device2.
        simulation.disconnectDevices(device1Label, device2Label);

        // peek inside device1 and device2 and verify that they are disconnected
        // (the number of input streams is < the number of devices on the network).
        List<PipedInputStream> inputStreams1 = Whitebox.getInternalState(device1, "inputStreams");
        List<PipedInputStream> inputStreams2 = Whitebox.getInternalState(device2, "inputStreams");
        assertEquals(NUM_DEVICES - 2, inputStreams1.size());  // there should be <NUM_DEVICES - self - disconnected devices> connections.
        assertEquals(NUM_DEVICES - 2, inputStreams2.size());

        // reconnect device1 and device2.
        simulation.connectDevices(device1Label, device2Label);

        // peek inside device1 and device2 and verify that they are disconnected
        // (the number of input streams is < the number of devices on the network).
        inputStreams1 = Whitebox.getInternalState(device1, "inputStreams");
        inputStreams2 = Whitebox.getInternalState(device2, "inputStreams");
        assertEquals(NUM_DEVICES - 1, inputStreams1.size());  // there should be <NUM_DEVICES - self> connections.
        assertEquals(NUM_DEVICES - 1, inputStreams2.size());

        // assert that we can send messages between device1 and device2
        device1.broadcast(barkPacket);
        device2.broadcast(barkPacket);
        final BarkPacket receivedPacket1 = device1.receive();
        final BarkPacket receivedPacket2 = device2.receive();
        assertEquals(this.barkPacket, receivedPacket1);
        assertEquals(this.barkPacket, receivedPacket2);
    }

    @Test
    public void testConnect_connectDeviceToItself_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> simulation.connectDevices(deviceLabels.get(0), deviceLabels.get(0)));
    }
}
