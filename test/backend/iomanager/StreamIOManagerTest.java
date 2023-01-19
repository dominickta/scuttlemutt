package backend.iomanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.BarkPacket;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class StreamIOManagerTest {
    // test constants
    private static final int NUM_STREAM_PAIRS = 3;  // the number of PipedInputStreams generated + stored in
                                                          // `inputStreams`.  MUST BE >= 2 OR TESTS WILL BREAK.
    // these constants represent the streamPairList indices used to access certain stream pairs for these tests.
    private static final int MANAGER_STREAM_PAIR = 0;
    private static final int NONMANAGER_INPUT_STREAM_PAIR_1 = 1;
    private static final int NONMANAGER_INPUT_STREAM_PAIR_2 = 2;

    // test variables
    private List<Pair<PipedInputStream, PipedOutputStream>> streamPairList;  // each "Pair" contains connected
                                                                             // PipedInput/OutputStreams.  Whatever is
                                                                             // fed to the PipedInputStream should be
                                                                             // available to read from the connected
                                                                             // PipedOutputStream.
    private StreamIOManager streamIOManager;
    private BarkPacket barkPacket1, barkPacket2;


    @BeforeEach
    public void setup() {
        // generate the stream pairs.
        this.streamPairList = Stream.generate(this::getPipedStreamPair)
                .limit(NUM_STREAM_PAIRS)
                .collect(Collectors.toList());

        // get an array of the non-manager PipedInputStreams.
        final PipedInputStream[] nonManagerInputStreams = new PipedInputStream[NUM_STREAM_PAIRS - 1];
        for (int i = 1; i < NUM_STREAM_PAIRS; i++) {
            nonManagerInputStreams[i - 1] = streamPairList.get(i).getLeft();
        }

        // create the StreamIOManager using the streams.
        this.streamIOManager = new StreamIOManager(nonManagerInputStreams, streamPairList.get(MANAGER_STREAM_PAIR).getRight());

        // create the BarkPackets used in testing.
        final byte[] packet1Contents = RandomStringUtils.randomAlphanumeric(15).getBytes();
        this.barkPacket1 = new BarkPacket(packet1Contents);

        final byte[] packet2Contents = RandomStringUtils.randomAlphanumeric(15).getBytes();
        this.barkPacket2 = new BarkPacket(packet2Contents);

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
    public void testBroadcast_barkPacketSuccessfullyWrittenToOutputStream() {
        // broadcast the packet.
        this.streamIOManager.broadcast(this.barkPacket1);

        // obtain the packet bytes from outputStream + verify that they look as expected.
        final byte[] outputBarkPacketBytes;
        try {
            final PipedInputStream inputStream = this.streamPairList.get(MANAGER_STREAM_PAIR)
                    .getLeft();
            int availableBytes = inputStream.available();
            outputBarkPacketBytes = inputStream.readNBytes(availableBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertArrayEquals(this.barkPacket1.getPacketContents(), outputBarkPacketBytes);
    }

    @Test
    public void testReceive_sendPacketToOneStream_barkPacketSuccessfullyReceived() {
        // write a packet to the first PipedInputStream.
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1)
                    .getRight();
            outputStream.write(barkPacket1.getPacketContents());
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
        // write a packet to the first and second PipedInputStream.
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_1)
                    .getRight();
            outputStream.write(barkPacket1.getPacketContents());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            final PipedOutputStream outputStream = this.streamPairList.get(NONMANAGER_INPUT_STREAM_PAIR_2)
                    .getRight();
            outputStream.write(barkPacket2.getPacketContents());
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // obtain the packets via receive and verify that both were received.
        final Set<BarkPacket> receivedBarkPackets = new HashSet<BarkPacket>();
        receivedBarkPackets.add(this.streamIOManager.receive());
        receivedBarkPackets.add(this.streamIOManager.receive());
        assertTrue(receivedBarkPackets.contains(barkPacket1));
        assertTrue(receivedBarkPackets.contains(barkPacket2));
    }

    /**
     * Returns a Pair containing connected PipedInputStream + PipedOutputStream objects.
     *
     * Make sure to use this helper method:  in order to use either of these objects, they must be connected in the
     * first place.  Otherwise, an IOException is thrown during execution.
     *
     * @return  a Pair containing connected PipedInputStream + PipedOutputStream objects.
     */
    private Pair<PipedInputStream, PipedOutputStream> getPipedStreamPair() {
        // create the streams.
        final PipedInputStream inputStream = new PipedInputStream();
        final PipedOutputStream outputStream = new PipedOutputStream();

        // connect the streams.
        try {
            inputStream.connect(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // return the Pair of streams.
        return new Pair<PipedInputStream, PipedOutputStream>() {
            @Override
            public PipedInputStream getLeft() {
                return inputStream;
            }

            @Override
            public PipedOutputStream getRight() {
                return outputStream;
            }

            @Override
            public PipedOutputStream setValue(PipedOutputStream value) {
                return null;
            }
        };
    }
}