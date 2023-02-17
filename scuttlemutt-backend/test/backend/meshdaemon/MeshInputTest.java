package backend.meshdaemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.PrivateKey;
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
import crypto.Crypto;
import storagemanager.MapStorageManager;
import storagemanager.StorageManager;
import types.Bark;
import types.TestUtils;
import types.packet.BarkPacket;
import types.packet.Packet;

/**
 * Runs tests for MeshInput.
 *
 * Tests for:
 * - Sending a bark that hasn't been seen.
 * - Sending a bark that has been seen (via altering seenBarks).
 * - Sending a bark twice.
 * - Sending many barks, mixed seen and not seen, mixed repeated.
 */
public class MeshInputTest {
    // Max should be strictly larger than min.
    private final int MIN_MULTIPACKET_COUNT = 10;
    private final int MAX_MULTIPACKET_COUNT = 20;

    private QueueIOManager ioManager;
    private BlockingQueue<Packet> inputQueue;

    private BlockingQueue<Bark> meshQueue;
    private Set<Bark> seenBarks;
    private MeshInput meshInput;

    /**
     * Sets up a single meshInput object with all the appropriate internals.
     */
    @BeforeEach
    public void setup() {
        // Set up test IOManager
        this.ioManager = new QueueIOManager();
        final String connectionLabel = "Connection-" + RandomStringUtils.randomAlphanumeric(15);
        this.inputQueue = new LinkedBlockingQueue<>(); // Only care about IOManager input
        this.ioManager.connect(connectionLabel, inputQueue, new LinkedBlockingQueue<>());

        this.meshQueue = new LinkedBlockingQueue<>();
        this.seenBarks = new HashSet<>();
        PrivateKey privateKey = Crypto.BOB_KEYPAIR.getPrivate();
        StorageManager storage = new MapStorageManager();

        this.meshInput = new MeshInput(ioManager, meshQueue, storage, privateKey, Crypto.DUMMY_SECRETKEY, seenBarks);
    }

    /**
     * Tests that a new barkPacket will be passed through.
     */
    @Test
    public void test_singleBark_notSeenPreviously() {
        // Queue up a single packet
        BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
        Bark bark = barkPacket.packetBarks.get(0);
        this.inputQueue.add(barkPacket);

        // Allow meshInput to grab the new packet
        this.meshInput.handleInput();

        // Check that the bark made is through
        assertEquals(bark, meshQueue.poll());
    }

    /**
     * Tests that previously seen barkPacket will be dropped.
     */
    @Test
    public void test_singleBark_seenPreviously() {
        // Queue up a single packet
        BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
        Bark bark = barkPacket.packetBarks.get(0);
        this.inputQueue.add(barkPacket);

        // Mark this patcket as previously seen
        this.seenBarks.add(bark);

        // Allow meshInput to grab the new packet
        this.meshInput.handleInput();

        // Check that the bark was dropped
        assertEquals(null, meshQueue.poll());
    }

    @Test
    public void test_singleBark_sentTwice() {
        // Queue up a single packet
        BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();
        Bark bark = barkPacket.packetBarks.get(0);
        this.inputQueue.add(barkPacket);

        // Allow meshInput to grab the new packet
        this.meshInput.handleInput();

        // Check that the bark made it through
        assertEquals(bark, meshQueue.poll());

        // queue the packet again
        this.inputQueue.add(barkPacket);

        // Allow meshInput to grab the repeated packet
        this.meshInput.handleInput();

        // Check that the bark was dropped
        assertEquals(null, meshQueue.poll());
    }

    @Test
    public void test_multiBark_mixedSend() {
        // Build a random number of packets
        final int num_packets = ThreadLocalRandom.current().nextInt(MIN_MULTIPACKET_COUNT, MAX_MULTIPACKET_COUNT + 1);
        List<BarkPacket> packets = new ArrayList<>();
        for (int i = 0; i < num_packets; i++) {
            packets.add(TestUtils.generateRandomizedBarkPacket());
        }

        // Mark half of the packets as seen before and shuffle
        for (int i = 0; i < num_packets / 2; i++) {
            seenBarks.add(packets.get(i).packetBarks.get(0));
        }
        Collections.shuffle(packets);

        // Re-add half of the packets for repeats and shuffle
        for (int i = 0; i < num_packets / 2; i++) {
            packets.add(packets.get(i));
        }
        Collections.shuffle(packets);

        // Add all packets to the queue
        for (BarkPacket packet : packets) {
            this.inputQueue.add(packet);
        }

        // Run processing for each packet
        for (int i = 0; i < packets.size(); i++) {
            this.meshInput.handleInput();
        }

        // Check all outputs and make sure there are no repeats
        Set<Bark> outputs = new HashSet<>();
        Bark nextBark = this.meshQueue.poll();
        while (nextBark != null) {
            assertTrue(outputs.add(nextBark));
            nextBark = this.meshQueue.poll();
        }

        // Make sure that we got the expected size (and ignored the marked packets)
        assertEquals(num_packets - num_packets / 2, outputs.size());
    }
}
