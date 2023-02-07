package backend.iomanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import types.TestUtils;
import types.packet.BarkPacket;
import types.packet.KeyExchangePacket;
import types.packet.Packet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;;

/**
 * Runs tests for the QueueIOManager.
 *
 * Tests are a combination of single- and mutli- manager, connection, and packet.
 */
public class QueueIOManagerTest {
    private final int NUM_PACKETS_FOR_MULTI = 10;
    private final int NUM_CONNECTIONS_FOR_MULTI = 5;

    /**
     * Tests that a single QueueIOManager can properly send a message.
     */
    @Test
    public void testSend_singleManager_singleConnection_singlePacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
            BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

            m.connect(connectionLabel, inputQueue, outputQueue);

            BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();

            m.send(connectionLabel, barkPacket);

            assertEquals(barkPacket, outputQueue.poll());

        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a single QueueIOManager can properly send multiple messages.
     */
    @Test
    public void testSend_singleManager_singleConnection_multiPacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
            BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

            m.connect(connectionLabel, inputQueue, outputQueue);

            List<BarkPacket> barkPackets = new ArrayList<>();

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                m.send(connectionLabel, barkPacket);
            }

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), outputQueue.poll());
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a single QueueIOManager can properly send multiple messages across many connections.
     */
    @Test
    public void testSend_singleManager_multiConnection_multiPacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            List<String> connectionLabels = new ArrayList<>();
            List<BlockingQueue<Packet>> outputQueues = new ArrayList<BlockingQueue<Packet>>();

            for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
                final String connectionLabel = "Connection" + i + "-" + RandomStringUtils.randomAlphanumeric(15);
                connectionLabels.add(connectionLabel);

                BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
                BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

                outputQueues.add(outputQueue);
                m.connect(connectionLabel, inputQueue, outputQueue);
            }

            for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
                List<BarkPacket> barkPackets = new ArrayList<>();

                for (int bi = 0; bi < this.NUM_PACKETS_FOR_MULTI; bi++) {
                    BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                    barkPackets.add(barkPacket);
                    m.send(connectionLabels.get(i), barkPacket);
                }

                // Check all queues, only one should contain the packets.
                for (int j = 0; j < this.NUM_CONNECTIONS_FOR_MULTI; j++) {
                    if (i == j) {
                        for (int bi = 0; bi < this.NUM_PACKETS_FOR_MULTI; bi++) {
                            assertEquals(barkPackets.get(bi), outputQueues.get(j).poll());
                        }
                    } else {
                        assertEquals(null, outputQueues.get(j).poll());
                    }
                }
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a single QueueIOManager can properly receive a message.
     */
    @Test
    public void testMeshReceive_singleManager_singleConnection_singlePacket() {
        QueueIOManager m = new QueueIOManager();

        final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

        BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
        BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

        m.connect(connectionLabel, inputQueue, outputQueue);

        List<Packet> barkPackets = new ArrayList<Packet>();

        for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
            BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
            barkPackets.add(barkPacket);
            inputQueue.add(barkPacket);
        }

        for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
            assertEquals(barkPackets.get(i), m.meshReceive(BarkPacket.class));
        }
    }

    /**
     * Tests that a single QueueIOManager can properly receive multiple messages.
     */
    @Test
    public void testMeshReceive_singleManager_singleConnection_multiPacket() {
        QueueIOManager m = new QueueIOManager();

        final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

        BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
        BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

        m.connect(connectionLabel, inputQueue, outputQueue);

        List<BarkPacket> barkPackets = new ArrayList<>();

        for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
            BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
            barkPackets.add(barkPacket);
            inputQueue.add(barkPacket);
        }

        for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
            assertEquals(barkPackets.get(i), m.meshReceive(BarkPacket.class));
        }
    }

    /**
     * Tests that a single QueueIOManager can properly receive multiple messages from many connections.
     */
    @Test
    public void testMeshReceive_singleManager_multiConnection_multiPacket() {
        QueueIOManager m = new QueueIOManager();

        List<String> connectionLabels = new ArrayList<>();
        List<BlockingQueue<Packet>> inputQueues = new ArrayList<BlockingQueue<Packet>>();

        for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
            final String connectionLabel = "Connection" + i + "-" + RandomStringUtils.randomAlphanumeric(15);
            connectionLabels.add(connectionLabel);

            BlockingQueue<Packet> inputQueue = new LinkedBlockingQueue<Packet>();
            BlockingQueue<Packet> outputQueue = new LinkedBlockingQueue<Packet>();

            inputQueues.add(inputQueue);
            m.connect(connectionLabel, inputQueue, outputQueue);
        }

        Set<BarkPacket> barkPackets = new HashSet<>();

        for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
            for (int bi = 0; bi < this.NUM_PACKETS_FOR_MULTI; bi++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                inputQueues.get(i).add(barkPacket);
            }
        }

        // Check that all messages exist.
        for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
            for (int bi = 0; bi < this.NUM_PACKETS_FOR_MULTI; bi++) {
                assertTrue(barkPackets.remove(m.meshReceive(BarkPacket.class)));
            }
        }
    }

    /**
     * Tests that two QueueIOManagers can properly send multiple messages back and forth.
     */
    @Test
    public void testSendRecieve_doubleManager_singleConnection_multiPacket() {
        try {
            QueueIOManager m1 = new QueueIOManager();
            QueueIOManager m2 = new QueueIOManager();

            final String connectionLabel1 = "Connection-m1-" + RandomStringUtils.randomAlphanumeric(15);
            final String connectionLabel2 = "Connection-m2-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<Packet> q1to2 = new LinkedBlockingQueue<Packet>();
            BlockingQueue<Packet> q2to1 = new LinkedBlockingQueue<Packet>();

            m1.connect(connectionLabel2, q2to1, q1to2);
            m2.connect(connectionLabel1, q1to2, q2to1);

            List<BarkPacket> barkPackets = new ArrayList<>();

            // m1 send to m2
            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                m1.send(connectionLabel2, barkPacket);
            }

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), m2.meshReceive(BarkPacket.class));
            }

            barkPackets = new ArrayList<>();

            // m2 send to m1
            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                m2.send(connectionLabel1, barkPacket);
            }
            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), m1.meshReceive(BarkPacket.class));
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSingleDeviceReceive_whenUndesiredPacketsAreInQueue_returnsDesiredType_thenCanReturnOtherTypes() {
        // link-up two QueueIOManagers.
        final QueueIOManager m1 = new QueueIOManager();

        final String connectionLabel = "Connection-m2-" + RandomStringUtils.randomAlphanumeric(15);

        final BlockingQueue<Packet> q1to2 = new LinkedBlockingQueue<Packet>();
        final BlockingQueue<Packet> q2to1 = new LinkedBlockingQueue<Packet>();

        m1.connect(connectionLabel, q2to1, q1to2);

        // load up q2to1 with two BarkPackets and then one KeyExchangePacket.
        final BarkPacket barkPacket1 = TestUtils.generateRandomizedBarkPacket();
        q2to1.add(barkPacket1);
        q2to1.add(TestUtils.generateRandomizedBarkPacket());
        final KeyExchangePacket kePacket = TestUtils.generateRandomizedKeyExchangePacket();
        q2to1.add(kePacket);

        // use m1 to get a KeyExchangePacket from m2.
        final KeyExchangePacket receivedKEPacket = m1.singleDeviceReceive(connectionLabel, KeyExchangePacket.class);

        // assert that the receivedPacket is equal to what was originally sent.
        assertEquals(kePacket, receivedKEPacket);

        // let's now verify that the BarkPackets we originally iterated over are still present and were not dropped.

        // use m1 to get a BarkPacket from m2.
        final BarkPacket receivedBarkPacket = m1.singleDeviceReceive(connectionLabel, BarkPacket.class);

        // assert that the obtained BarkPacket is identical to barkPacket1.
        assertEquals(barkPacket1, receivedBarkPacket);
    }
}
