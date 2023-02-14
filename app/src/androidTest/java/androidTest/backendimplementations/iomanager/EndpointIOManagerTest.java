package androidTest.backendimplementations.iomanager;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import androidx.test.filters.SmallTest;

import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.scuttlemutt.app.backendimplementations.iomanager.EndpointIOManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import backend.iomanager.IOManagerException;
import types.TestUtils;
import types.packet.BarkPacket;
import types.packet.KeyExchangePacket;

/**
 * Runs tests for the EndpointIOManager.
 */
@RunWith(MockitoJUnitRunner.class)
@SmallTest
public class EndpointIOManagerTest {
    // test objects
    private BarkPacket barkPacket1, barkPacket2;
    private KeyExchangePacket kePacket;
    private String otherDeviceId1, otherDeviceId2;  // identifies the other parties on the connection.
    private ConnectionsClient mockedConnectionsClient;
    private EndpointIOManager ioManager;

    @Before
    public void setup() {
        // setup the packets + otherDeviceId.
        this.barkPacket1 = TestUtils.generateRandomizedBarkPacket();
        this.barkPacket2 = TestUtils.generateRandomizedBarkPacket();
        this.kePacket = TestUtils.generateRandomizedKeyExchangePacket();
        this.otherDeviceId1 = RandomStringUtils.randomAlphanumeric(15);
        this.otherDeviceId2 = RandomStringUtils.randomAlphanumeric(15);

        // setup the mocked ConnectionsClient.
        this.mockedConnectionsClient = Mockito.mock(ConnectionsClient.class);

        // create the EndpointIOManager containing the mockedConnectionsClient.
        this.ioManager = new EndpointIOManager(this.mockedConnectionsClient);

        // store otherDeviceId1/2 as valid connections for the IOManager.
        final Set<String> validConns = new HashSet<String>();
        validConns.add(this.otherDeviceId1);
        validConns.add(this.otherDeviceId2);
        this.ioManager.updateAvailableConnection(validConns);
    }

    /**
     * Tests that an EndpointIOManager can successfully send a message.
     */
    @Test
    public void testSend_successfullySendsPacketToReceiver() {
        // send the packet.
        try {
            this.ioManager.send(this.otherDeviceId1, this.barkPacket1);
        } catch (IOManagerException e) {
            throw new RuntimeException(e);
        }

        // assert that the mockedConnectionsClient received the packet as expected.
        final Payload p = Payload.fromBytes(this.barkPacket1.toNetworkBytes());
        this.assertMockedConnectionsClientSendPayloadCall(this.otherDeviceId1, p);
    }

    /**
     * Tests that an EndpointIOManager fails when trying to send a packet to an invalid receiver.
     */
    @Test
    public void testSend_invalidReceiver_failsToSendPacket() {
        // create an invalid deviceId for the receiver.
        final String invalidReceiverId = RandomStringUtils.randomAlphanumeric(15);

        // assert that an exception is thrown when a packet is sent to an invalid deviceId.
        assertThrows(IOManagerException.class, () -> this.ioManager.send(invalidReceiverId, this.barkPacket1));
    }

    /**
     * Tests that an EndpointIOManager can successfully receive a message of the desired type using
     * meshReceive().
     */
    @Test
    public void testMeshReceive_onlyBarkPacketInQueue_receiveBarkPacket_successfullyReceivesBarkPacket() {
        // ingest the BarkPacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.barkPacket1);

        // receive the packet using meshReceive().
        final BarkPacket receivedPacket = this.ioManager.meshReceive(BarkPacket.class);

        // verify that the received packet matches what we sent to the ioManager.
        assertEquals(this.barkPacket1, receivedPacket);
    }

    /**
     * Tests that an EndpointIOManager can successfully receive packets from multiple devices using
     * meshReceive().
     */
    @Test
    public void testMeshReceive_packetsFromMultipleDevices_receiveBarkPacketTwice_successfullyReceivesBothPackets() {
        // ingest the BarkPackets into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.barkPacket1);
        this.ioManager.addReceivedMessage(this.otherDeviceId2, this.barkPacket2);

        // receive the packets via two calls to meshReceive().
        final Set<BarkPacket> receivedPackets = new HashSet<BarkPacket>();
        receivedPackets.add(this.ioManager.meshReceive(BarkPacket.class));
        receivedPackets.add(this.ioManager.meshReceive(BarkPacket.class));

        // verify that the packets we sent to the ioManager are among the packages we received from
        // the ioManager.
        assertTrue(receivedPackets.contains(this.barkPacket1));
        assertTrue(receivedPackets.contains(this.barkPacket2));
    }

    /**
     * Tests that an EndpointIOManager can call `meshReceive()` multiple times and successfully
     * receive packets of different types from the queue.
     */
    @Test
    public void testMeshReceive_whenUndesiredPacketsAreInQueue_returnsDesiredType_thenCanReturnOtherTypes() {
        // ingest a KeyExchangePacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.kePacket);

        // ingest a BarkPacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.barkPacket1);

        // receive the BarkPacket using meshReceive().  Even though the KeyExchangePacket is ahead
        // of the BarkPacket in the queue of messages, we should receive the BarkPacket.
        final BarkPacket receivedBarkPacket = this.ioManager.meshReceive(BarkPacket.class);

        // verify that the received BarkPacket matches what we sent to the this.ioManager.
        assertEquals(this.barkPacket1, receivedBarkPacket);

        // receive the KeyExchangePacket using meshReceive().  Since we received the BarkPacket
        // earlier, the KeyExchangePacket should still be present in the internal queue of messages.
        final KeyExchangePacket receivedKEPacket = this.ioManager.meshReceive(KeyExchangePacket.class);

        // verify that the received receivedKEPacket matches what we sent to the this.ioManager.
        assertEquals(this.kePacket, receivedKEPacket);
    }

    /**
     * Tests that an EndpointIOManager can successfully receive a message of the desired type using
     * singleDeviceReceive().
     */
    @Test
    public void testSingleDeviceReceive_onlyBarkPacketInQueue_receiveBarkPacket_successfullyReceivesBarkPacket() {
        // ingest the BarkPacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.barkPacket1);

        // receive the packet using singleDeviceReceive().
        final BarkPacket receivedPacket = this.ioManager.singleDeviceReceive(this.otherDeviceId1, BarkPacket.class);

        // verify that the received packet matches what we sent to the ioManager.
        assertEquals(this.barkPacket1, receivedPacket);
    }

    /**
     * Tests that an EndpointIOManager can call `singleDeviceReceive()` multiple times and successfully
     * receive packets of different types from the queue.
     */
    @Test
    public void testSingleDeviceReceive_whenUndesiredPacketsAreInQueue_returnsDesiredType_thenCanReturnOtherTypes() {
        // ingest a KeyExchangePacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.kePacket);

        // ingest a BarkPacket into the ioManager.
        this.ioManager.addReceivedMessage(this.otherDeviceId1, this.barkPacket1);

        // receive the BarkPacket using singleDeviceReceive().  Even though the KeyExchangePacket is ahead
        // of the BarkPacket in the queue of messages, we should receive the BarkPacket.
        final BarkPacket receivedBarkPacket = this.ioManager.singleDeviceReceive(this.otherDeviceId1, BarkPacket.class);

        // verify that the received BarkPacket matches what we sent to the ioManager.
        assertEquals(this.barkPacket1, receivedBarkPacket);

        // receive the KeyExchangePacket using singleDeviceReceive().  Since we received the BarkPacket
        // earlier, the KeyExchangePacket should still be present in the internal queue of messages.
        final KeyExchangePacket receivedKEPacket = this.ioManager.singleDeviceReceive(this.otherDeviceId1, KeyExchangePacket.class);

        // verify that the received receivedKEPacket matches what we sent to the ioManager.
        assertEquals(this.kePacket, receivedKEPacket);
    }

    /**
     * Asserts that the mockedConnectionsClient's `sendPayload()` method was called with the
     * specified params.
     *
     * @param s  The expected `s` String parameter passed into `sendPayload()`
     * @param p  The expected `p` Payload parameter passed into `sendPayload()`
     */
    private void assertMockedConnectionsClientSendPayloadCall(final String s, final Payload p) {
        // create the ArgumentCaptors used to analyze the content input into the mockedConnectionsClient.
        final ArgumentCaptor<String> sArgCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Payload> pArgCaptor = ArgumentCaptor.forClass(Payload.class);

        // capture the input.
        verify(this.mockedConnectionsClient).sendPayload(sArgCaptor.capture(), pArgCaptor.capture());

        // verify that the captured input matches the expected input.
        assertEquals(s, sArgCaptor.getValue());
        // Payload's `equals()` is not correctly implemented, so we have to compare byte[]s instead.
        assertArrayEquals(p.asBytes(), pArgCaptor.getValue().asBytes());
    }
}
