package backend.iomanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import types.BarkPacket;
import types.TestUtils;

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

            BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();

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

            BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();

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
            List<BlockingQueue<BarkPacket>> outputQueues = new ArrayList<>();

            for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
                final String connectionLabel = "Connection" + i + "-" + RandomStringUtils.randomAlphanumeric(15);
                connectionLabels.add(connectionLabel);
    
                BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
                BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();
    
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
    public void testReceive_singleManager_singleConnection_singlePacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();

            m.connect(connectionLabel, inputQueue, outputQueue);

            List<BarkPacket> barkPackets = new ArrayList<>();

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                inputQueue.add(barkPacket);
            }

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), m.receive());
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a single QueueIOManager can properly receive multiple messages.
     */
    @Test
    public void testReceive_singleManager_singleConnection_multiPacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();

            m.connect(connectionLabel, inputQueue, outputQueue);

            List<BarkPacket> barkPackets = new ArrayList<>();

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                inputQueue.add(barkPacket);
            }

            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), m.receive());
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests that a single QueueIOManager can properly receive multiple messages from many connections.
     */
    @Test
    public void testReceive_singleManager_multiConnection_multiPacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            List<String> connectionLabels = new ArrayList<>();
            List<BlockingQueue<BarkPacket>> inputQueues = new ArrayList<>();

            for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
                final String connectionLabel = "Connection" + i + "-" + RandomStringUtils.randomAlphanumeric(15);
                connectionLabels.add(connectionLabel);
    
                BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
                BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();
    
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

            // Check that all messages exists.
            for (int i = 0; i < this.NUM_CONNECTIONS_FOR_MULTI; i++) {
                for (int bi = 0; bi < this.NUM_PACKETS_FOR_MULTI; bi++) {
                    assertTrue(barkPackets.remove(m.receive()));
                }
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
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

            BlockingQueue<BarkPacket> q1to2 = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> q2to1 = new LinkedBlockingQueue<>();

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
                assertEquals(barkPackets.get(i), m2.receive());
            }

            barkPackets = new ArrayList<>();

            // m2 send to m1
            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
                barkPackets.add(barkPacket);
                m2.send(connectionLabel1, barkPacket);
            }
            for (int i = 0; i < this.NUM_PACKETS_FOR_MULTI; i++) {
                assertEquals(barkPackets.get(i), m1.receive());
            }
        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }
}
