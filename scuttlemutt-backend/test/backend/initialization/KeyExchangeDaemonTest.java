package backend.initialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.crypto.SecretKey;

import backend.iomanager.QueueIOManager;
import crypto.Crypto;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.DawgIdentifier;
import types.TestUtils;
import types.packet.KeyExchangePacket;
import types.packet.Packet;

public class KeyExchangeDaemonTest {

    // test objects.
    DawgIdentifier myDawgId, otherDawgId1, otherDawgId2;
    BlockingQueue<Packet> qMeTo1, q1ToMe, qMeTo2, q2ToMe;
    KeyExchangeDaemon keyExchangeDaemon;
    StorageManager storageManager;
    PublicKey myPublicKey, otherPublicKey1, otherPublicKey2;
    SecretKey otherSecretKey1, otherSecretKey2;
    KeyExchangePacket otherKePacket1, otherKePacket2;

    @BeforeEach
    public void setup() {
        // setup QueueIOManager infra (a manager w/ one set of connections) for testing.
        final QueueIOManager ioManager = new QueueIOManager();
        this.otherDawgId1 = TestUtils.generateRandomizedDawgIdentifier();
        this.otherDawgId2 = TestUtils.generateRandomizedDawgIdentifier();
        this.qMeTo1 = new LinkedBlockingQueue<Packet>();
        this.q1ToMe = new LinkedBlockingQueue<Packet>();
        this.qMeTo2 = new LinkedBlockingQueue<Packet>();
        this.q2ToMe = new LinkedBlockingQueue<Packet>();
        ioManager.connect(otherDawgId1.getUsername(), q1ToMe, qMeTo1);
        ioManager.connect(otherDawgId2.getUsername(), q2ToMe, qMeTo2);

        // initialize all other objects needed by KeyExchangeDaemon.
        this.myPublicKey = Crypto.generateKeyPair().getPublic();
        this.myDawgId = TestUtils.generateRandomizedDawgIdentifier();
        this.storageManager = new MapStorageManager();

        // create the KeyExchangeDaemon.
        this.keyExchangeDaemon = new KeyExchangeDaemon(ioManager,
                this.storageManager,
                this.myPublicKey,
                this.myDawgId);

        // create KeyExchangePacket objects used for testing.
        this.otherSecretKey1 = Crypto.generateSecretKey();
        this.otherSecretKey2 = Crypto.generateSecretKey();
        this.otherPublicKey1 = Crypto.generateKeyPair().getPublic();
        this.otherPublicKey2 = Crypto.generateKeyPair().getPublic();
        otherKePacket1 = new KeyExchangePacket(this.otherPublicKey1,
                this.otherSecretKey1,
                this.otherDawgId1);
        otherKePacket2 = new KeyExchangePacket(this.otherPublicKey2,
                this.otherSecretKey2,
                this.otherDawgId2);
    }

    @Test
    public void testExchangeKeys_successfullyExchangesKeys() {
        // verify that no exchange is ongoing.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.NO_EXCHANGE,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // send otherKePacket to the device.
        this.q1ToMe.add(otherKePacket1);

        // start the exchange in the daemon.
        this.keyExchangeDaemon.exchangeKeys(this.otherDawgId1.getUsername());

        // wait 1s.
        TestUtils.sleepOneSecond();

        // verify that the exchange completed successfully.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.COMPLETED_SUCCESSFULLY,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // obtain the packet we sent to the other device.
        final KeyExchangePacket myPacket = (KeyExchangePacket) qMeTo1.remove();

        // verify that the correct SecretKey was stored.
        verifyCorrectSecretKeyStored(myPacket, this.otherSecretKey1, this.otherDawgId1);

        // verify that the other device's PublicKey was stored.
        final PublicKey storedPublicKey = this.storageManager.lookupPublicKeyForUUID(this.otherDawgId1.getUUID());
        assertEquals(this.otherPublicKey1, storedPublicKey);
    }

    @Test
    public void testExchangeKeys_receiveTimeouts_storesFailureState() {
        // verify that no exchange is ongoing.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.NO_EXCHANGE,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // start the exchange in the daemon.
        // no packet is being sent to the device, so the daemon should timeout
        // on singleDeviceReceive().
        this.keyExchangeDaemon.exchangeKeys(this.otherDawgId1.getUsername());

        // verify that the exchange is in-progress.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.IN_PROGRESS,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // wait 6s (5s for timeout + 1s for good measure).
        for (int i = 0; i < 6; i++) {
            TestUtils.sleepOneSecond();
        }

        // verify that the exchange failed.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.FAILED,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));
    }

    @Test
    public void testExchangeKeys_exchangeAlreadyInProgressForDevice_throwsRuntimeException() {
        // verify that no exchange is ongoing.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.NO_EXCHANGE,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // start the exchange in the daemon.
        // no packet is being sent to the device, so the daemon should not complete its call to
        // on singleDeviceReceive().
        this.keyExchangeDaemon.exchangeKeys(this.otherDawgId1.getUsername());

        // verify that the exchange is in-progress.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.IN_PROGRESS,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));

        // call exchangeKeys() a second time.  a RuntimeException should be thrown.
        assertThrows(RuntimeException.class,
                () -> this.keyExchangeDaemon.exchangeKeys(this.otherDawgId1.getUsername()));
    }

    @Test
    public void testExchangeKeys_multipleCallsExecuteAtOnce_successfullyExchangesKeysForBothCalls() {
        // verify that no exchange is ongoing for both connected devices.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.NO_EXCHANGE,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.NO_EXCHANGE,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId2.getUsername()));

        // start both exchanges in the daemon.  since no packets exist in either queue, they will
        // be executing simultaneously.
        this.keyExchangeDaemon.exchangeKeys(this.otherDawgId1.getUsername());
        this.keyExchangeDaemon.exchangeKeys(this.otherDawgId2.getUsername());

        // verify that keys are now being exchanged with both devices.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.IN_PROGRESS,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.IN_PROGRESS,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId2.getUsername()));

        // send otherKePacket to our device from both connected devices.
        this.q1ToMe.add(otherKePacket1);
        this.q2ToMe.add(otherKePacket2);

        // wait 1s.
        TestUtils.sleepOneSecond();

        // verify that the exchange completed successfully.
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.COMPLETED_SUCCESSFULLY,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId1.getUsername()));
        assertEquals(KeyExchangeDaemon.KEY_EXCHANGE_STATUS.COMPLETED_SUCCESSFULLY,
                this.keyExchangeDaemon.getKeyExchangeStatus(this.otherDawgId2.getUsername()));

        // obtain the packet we sent to the other device.
        final KeyExchangePacket myPacketTo1 = (KeyExchangePacket) qMeTo1.remove();
        final KeyExchangePacket myPacketTo2 = (KeyExchangePacket) qMeTo2.remove();

        // verify that the correct SecretKeys were stored for 1 and 2.
        verifyCorrectSecretKeyStored(myPacketTo1, this.otherSecretKey1, this.otherDawgId1);
        verifyCorrectSecretKeyStored(myPacketTo2, this.otherSecretKey2, this.otherDawgId2);

        // verify that the other devices' PublicKeys were stored.
        final PublicKey storedPublicKey1 = this.storageManager.lookupPublicKeyForUUID(this.otherDawgId1.getUUID());
        final PublicKey storedPublicKey2 = this.storageManager.lookupPublicKeyForUUID(this.otherDawgId2.getUUID());
        assertEquals(this.otherPublicKey1, storedPublicKey1);
        assertEquals(this.otherPublicKey2, storedPublicKey2);
    }

    /**
     * Verifies that the correct SecretKey was stored in the exchange.
     *
     * ("Correct" == the packet stored was the one with the lower hashcode.  The packets are
     * originally generated on each device and are stored during the exchange.)
     *
     * @param sentPacket The KeyExchangePacket originally sent to the other device in the exchange.
     * @param otherSecretKey  The SecretKey sent by the other device in the exchange to this device.
     * @param otherDawgId The DawgIdentifier associated with the other device in the exchange.
     */
    private void verifyCorrectSecretKeyStored(final KeyExchangePacket sentPacket,
                                              final SecretKey otherSecretKey,
                                              final DawgIdentifier otherDawgId) {
        final SecretKey sentSecretKeyTo1 = sentPacket.getSecretKey();
        final SecretKey expectedSecretKey1;
        if (sentSecretKeyTo1.hashCode() < otherSecretKey.hashCode()) {
            expectedSecretKey1 = otherSecretKey;
        } else {
            expectedSecretKey1 = sentSecretKeyTo1;
        }
        final SecretKey storedSecretKey1 = this.storageManager.lookupLatestSecretKeyForUuid(otherDawgId.getUUID());
        assertEquals(expectedSecretKey1, storedSecretKey1);
    }
}
