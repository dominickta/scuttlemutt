package backend.meshdaemon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.iomanager.QueueIOManager;
import types.Bark;
import types.TestUtils;
import types.packet.BarkPacket;
import types.packet.Packet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Runs tests for MeshOutput.
 *
 * Tests for:
 * - Sending single a bark
 * - Sending multiple barks, in order
 */
public class MeshOutputTest {
    // Max should be strictly larger than min.
    private final int MIN_MULTIPACKET_COUNT = 10;
    private final int MAX_MULTIPACKET_COUNT = 20;

    private QueueIOManager ioManager;
    private Set<BlockingQueue<Packet>> outputQueues;

    private BlockingQueue<Bark> meshQueue;
    private Set<Bark> seenBarks;
    private MeshOutput meshOutput;

    /**
     * Sets up a single MeshOutput object with all the appropriate internals.
     */
    @BeforeEach
    public void setup() {
        this.ioManager = new QueueIOManager();
        this.outputQueues = new HashSet<>(); // Only care about IOManager outputs

        for (int i = 0; i < MeshOutput.NUM_REBROADCAST_BEFORE_DROP; i++) {
            String connectionLabel = "Connection" + i + "-" + RandomStringUtils.randomAlphanumeric(15);
            BlockingQueue outputQueue = new LinkedBlockingQueue<>();
            this.outputQueues.add(outputQueue);
            this.ioManager.connect(connectionLabel, new LinkedBlockingQueue<>(), outputQueue);
        }

        this.meshQueue = new LinkedBlockingQueue<>();
        this.seenBarks = new HashSet<>();

        this.meshOutput = new MeshOutput(ioManager, meshQueue, seenBarks);
    }

    /**
     * Tests that a new bark will be passed through and converted to barkPacket.
     */
    @Test
    public void test_singleBark() {
        // Queue up a single packet
        BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
        Bark bark = barkPacket.packetBarks.get(0);
        this.meshQueue.add(bark);

        // Allow MeshOutput to grab the new packet
        this.meshOutput.handleOutput();

        // Check that the bark made is through
        for (BlockingQueue<Packet> outputQueue : this.outputQueues) {
            assertEquals(barkPacket, outputQueue.poll());
        }

        // Check that the bark made is through
        assertTrue(seenBarks.contains(bark));
    }

    /**
     * Tests that a bunch of barks will be passed through and converted to barkPackets.
     * Ordering should be retained, all messages should go through, and seenBarks should
     * be updated for all.
     */
    @Test
    public void test_multiBark() {
        // Build a random number of packets
        final int num_packets = ThreadLocalRandom.current().nextInt(MIN_MULTIPACKET_COUNT, MAX_MULTIPACKET_COUNT + 1);
        List<BarkPacket> packets = new ArrayList<>();
        for (int i = 0; i < num_packets; i++) {
            packets.add(TestUtils.generateRandomizedBarkPacket());
        }

        // Mark half of the packets as seen before and shuffle
        for (int i = 0; i < num_packets/2; i++) {
            seenBarks.add(packets.get(i).packetBarks.get(0));
        }
        Collections.shuffle(packets);

        // Re-add half of the packets for repeats and shuffle
        for (int i = 0; i < num_packets/2; i++) {
            packets.add(packets.get(i));
        }
        Collections.shuffle(packets);

        // Add all packets to the queue
        for (BarkPacket packet : packets) {
            this.meshQueue.add(packet.packetBarks.get(0));
        }

        // Run processing for each packet
        for (int i = 0; i < packets.size(); i++) {
            this.meshOutput.handleOutput();
        }

        // Check that all bark packets made it through, in order
        for (BarkPacket barkPacket : packets) {
            assertTrue(this.seenBarks.contains(barkPacket.packetBarks.get(0)));
            for (BlockingQueue<Packet> outputQueue : this.outputQueues) {
                assertEquals(barkPacket, outputQueue.poll());
            }
        }
    }
}