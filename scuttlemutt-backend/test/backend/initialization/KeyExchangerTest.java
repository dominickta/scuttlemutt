package backend.initialization;

import backend.iomanager.QueueIOManager;
import crypto.Crypto;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.TestUtils;
import types.packet.KeyExchangePacket;
import types.packet.Packet;

import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyExchangerTest {

    // test objects.
    String otherDeviceId;
    BlockingQueue<Packet> q1to2, q2to1;
    KeyExchanger keyExchanger;

    @BeforeEach
    public void setup() {
        // setup QueueIOManager infra (a manager w/ one set of connections) for testing.
        final QueueIOManager m1 = new QueueIOManager();
        this.otherDeviceId = "Connection-m2-" + RandomStringUtils.randomAlphanumeric(15);
        this.q1to2 = new LinkedBlockingQueue<Packet>();
        this.q2to1 = new LinkedBlockingQueue<Packet>();
        m1.connect(otherDeviceId, q2to1, q1to2);

        // create the KeyExchanger using the above QueueIOManager + a new StorageManager.
        final StorageManager storageManager = new MapStorageManager();
        this.keyExchanger = new KeyExchanger(m1, storageManager);
    }

    @Test
    public void testSendPublicKey() {
        // create + send a public key using the keyExchanger.
        final PublicKey m1PublicKey = Crypto.generateKeyPair().getPublic();
        this.keyExchanger.sendPublicKey(m1PublicKey, otherDeviceId);

        // verify the packet sent by the KeyExchanger contains the PublicKey as expected.
        final KeyExchangePacket receivedKePacket = (KeyExchangePacket) q1to2.remove();
        assertEquals(m1PublicKey, receivedKePacket.getPublicKey());

    }

    @Test
    public void testReceivePublicKey() {
        // send a KeyExchange packet to m1.
        final KeyExchangePacket sentKePacket = TestUtils.generateRandomizedKeyExchangePacket();
        this.q2to1.add(sentKePacket);

        // receive the PublicKey using the KeyExchanger, verify that the generated DawgIdentifier is as expected.
        final DawgIdentifier dawgId = this.keyExchanger.receivePublicKey(this.otherDeviceId);
        assertEquals(sentKePacket.getPublicKey(), dawgId.getPublicKey());
    }
}
