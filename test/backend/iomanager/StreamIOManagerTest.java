package backend.iomanager;

import backend.simulation.PipedStreamHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.Bark;
import types.BarkPacket;
import types.TestUtils;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StreamIOManagerTest {
    // test constants
    private static final int NUM_STREAM_PAIRS = 3;  // the number of PipedInputStreams generated + stored in
                                                    // `streamPairList`.  MUST BE >= 2 OR TESTS WILL BREAK.

    // these constants represent the streamPairList indices used to access certain stream pairs for these tests.
    private static final int MANAGER_STREAM_PAIR = 0;
    private static final int NONMANAGER_INPUT_STREAM_PAIR_1 = 1;
    private static final int NONMANAGER_INPUT_STREAM_PAIR_2 = 2;

    // test variables
    private List<Pair<PipedInputStream, PipedOutputStream>> streamPairList;  // each "Pair" contains connected
                                                                             // PipedInput/OutputStreams.  Whatever is
                                                                             // fed to the PipedInputStream is
                                                                             // available to read from the connected
                                                                             // PipedOutputStream.
    private StreamIOManager streamIOManager;
    private BarkPacket barkPacket1, barkPacket2;


    @BeforeEach
    public void setup() {
        // generate the stream pairs.
        this.streamPairList = Stream.generate(PipedStreamHelper::getPipedStreamPair)
                .limit(NUM_STREAM_PAIRS)
                .collect(Collectors.toList());

        // create the StreamIOManager.
        this.streamIOManager = new StreamIOManager();

        // create the non-manager device strings.
        final String nonManager1 = RandomStringUtils.randomAlphanumeric(15);
        final String nonManager2 = RandomStringUtils.randomAlphanumeric(15);

        // hookup the non-manager devices to the streamIOManager.
        this.streamIOManager.connect(nonManager1, streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1).getLeft(),
                streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1).getRight());
        this.streamIOManager.connect(nonManager2, streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_2).getLeft(),
                streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_2).getRight());

        // create the BarkPackets used in testing.
        final List<Bark> packet1Barks = Collections.singletonList(TestUtils.generateRandomizedBark());
        this.barkPacket1 = new BarkPacket(packet1Barks);

        final List<Bark> packet2Barks = Collections.singletonList(TestUtils.generateRandomizedBark());
        this.barkPacket2 = new BarkPacket(packet2Barks);

    }

    @AfterEach
    public void cleanup() {
        // close the streams.
        try {
            for (final Pair<PipedInputStream, PipedOutputStream> p : streamPairList) {
                p.getLeft().close();
                p.getRight().close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testBroadcast_barkPacketSuccessfullyWrittenToAllOutputStreams() {
        // broadcast the packet.
        this.streamIOManager.broadcast(this.barkPacket1);

        // obtain the packet bytes from outputStream + verify that they look as expected.
        final byte[] outputBarkPacketBytes1, outputBarkPacketBytes2;
        try {
            // get the data output to the PipedOutputStream corresponding with nonManager1.
            final PipedInputStream inputStream1 = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1)
                    .getLeft();
            int availableBytes1 = inputStream1.available();
            outputBarkPacketBytes1 = inputStream1.readNBytes(availableBytes1);

            // get the data output to the PipedOutputStream corresponding with nonManager2.
            final PipedInputStream inputStream2 = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_2)
                    .getLeft();
            int availableBytes2 = inputStream2.available();
            outputBarkPacketBytes2 = inputStream2.readNBytes(availableBytes2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // verify that both packet bytes look as expected
        assertArrayEquals(this.barkPacket1.toNetworkBytes(), outputBarkPacketBytes1);
        assertArrayEquals(this.barkPacket1.toNetworkBytes(), outputBarkPacketBytes2);
    }

    @Test
    public void testReceive_sendPacketToOneStream_barkPacketSuccessfullyReceived() {
        // write a packet to the PipedInputStream corresponding to the first fake nonManager connection.
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1)
                    .getRight();
            outputStream.write(barkPacket1.toNetworkBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // obtain the packet from receive and verify that it looks as expected.
        final BarkPacket receivedBarkPacket = this.streamIOManager.receive();
        assertEquals(this.barkPacket1, receivedBarkPacket);
    }

    @Test
    public void testReceive_sendPacketsToTwoStreams_bothPacketSuccessfullyReceivedInRandomOrder() {
        // write a packet to the PipedInputStreams corresponding to the fake nonManager connections.
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1)
                    .getRight();
            outputStream.write(barkPacket1.toNetworkBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_2)
                    .getRight();
            outputStream.write(barkPacket2.toNetworkBytes());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // obtain the packets via receive and verify that both were received.
        final Set<BarkPacket> receivedBarkPackets = new HashSet<BarkPacket>();
        receivedBarkPackets.add(this.streamIOManager.receive());
        receivedBarkPackets.add(this.streamIOManager.receive());

        final Set<BarkPacket> expectedSet = new HashSet<BarkPacket>();
        expectedSet.add(barkPacket1);
        expectedSet.add(barkPacket2);

        assertEquals(expectedSet, receivedBarkPackets);
    }
}