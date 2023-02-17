package backend.initialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.powermock.reflect.Whitebox;

import backend.iomanager.QueueIOManager;
import crypto.Crypto;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.TestUtils;
import types.packet.KeyExchangePacket;
import types.packet.Packet;

public class KeyExchangerTest {

    // test objects.
    String otherDeviceId;
    BlockingQueue<Packet> q1to2, q2to1;
    KeyExchanger keyExchanger;
    StorageManager storageManager;

    @BeforeEach
    public void setup() {
        // setup QueueIOManager infra (a manager w/ one set of connections) for testing.
        final QueueIOManager m1 = new QueueIOManager();
        this.otherDeviceId = "Connection-m2-" + RandomStringUtils.randomAlphanumeric(15);
        this.q1to2 = new LinkedBlockingQueue<Packet>();
        this.q2to1 = new LinkedBlockingQueue<Packet>();
        m1.connect(otherDeviceId, q2to1, q1to2);

        // create the KeyExchanger using the above QueueIOManager + a new
        // StorageManager.
        storageManager = new MapStorageManager();
        this.keyExchanger = new KeyExchanger(m1, storageManager);
    }

    @Test
    public void testSendSecretKey() {
        // create + send a SecretKey using the KeyExchanger.
        this.keyExchanger.sendSecretKey(otherDeviceId);

        // verify that a KeyExchangePacket was successfully sent by the KeyExchanger.
        final Packet receivedKePacket = q1to2.remove();
        assertTrue(receivedKePacket instanceof KeyExchangePacket);
    }

    @Test
    public void testReceiveSecretKey() {
        // store a SecretKey for the otherDeviceId in the KeyExchanger.
        final Map<String, SecretKey> internalKeyExchangeMap = new HashMap<String, SecretKey>();
        final SecretKey localKey = Crypto.generateSecretKey();
        internalKeyExchangeMap.put(this.otherDeviceId, localKey);
        Whitebox.setInternalState(this.keyExchanger, "secretKeysBeingExchanged", internalKeyExchangeMap, KeyExchanger.class);

        // send a KeyExchange packet to m1.
        final KeyExchangePacket sentKePacket = TestUtils.generateRandomizedKeyExchangePacket();
        this.q2to1.add(sentKePacket);
        final SecretKey sentKey = (SecretKey) sentKePacket.getKey();

        // figure out which SecretKey we should expect to have stored (the one with the
        // lower hashCode).
        final SecretKey expectedKey = localKey.hashCode() < sentKey.hashCode() ? sentKey : localKey;

        // receive the SecretKey using the KeyExchanger, verify that the expectedKey is
        // stored.
        final DawgIdentifier dawgId = this.keyExchanger.receiveSecretKey(this.otherDeviceId,
                Crypto.ALICE_KEYPAIR.getPublic());
        final SecretKey storedKey = this.storageManager.lookupSecretKeyForPublicKey(dawgId.getPublicKey());
        assertEquals(expectedKey, storedKey);
    }
}
