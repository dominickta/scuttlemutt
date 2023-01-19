package backend.iomanager;

import types.BarkPacket;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * This class represents an InputStream + OutputStream-based I/O manager.
 *
 * This manager can be used to simulate a network via feeding the streams to different devices.
 */
public class StreamIOManager implements IOManager {
    // streams

    // stores the PipedInputStreams to pull data from. (These should be other StreamIOManagers.)
    private final List<PipedInputStream> inputStreams;
    // stores the PipedOutputStream to write data to.
    private final PipedOutputStream outputStream;

    /**
     * Constructs a StreamIOManager.
     * @param inputStreams  The streams from which data should be pulled.
     * @param outputStream  The stream to which the manager writes to.
     */
    public StreamIOManager(final List<PipedInputStream> inputStreams, final PipedOutputStream outputStream) {
        this.inputStreams = inputStreams;
        this.outputStream = outputStream;
    }

    @Override
    public void broadcast(BarkPacket packet) {
        try {
            this.outputStream.write(packet.getPacketContents());
            this.outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BarkPacket receive() {

        // return a packet obtained by the PipedInputStreamChecker.
        try {
            return new PipedInputStreamChecker().call();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stores the passed PipedInputStream in the StreamIOManager's list of incoming connections.  This is equivalent
     * to creating the connection between the two devices.
     *
     * @param inputStream  The stream being added to the StreamIOManager's incoming connections.
     */
    public void connect(final PipedInputStream inputStream) {
        this.inputStreams.add(inputStream);
    }

    /**
     * Removes the passed PipedInputStream from the StreamIOManager's incoming connections.  This is equivalent
     * to cutting the connection between the two devices.
     *
     * @param inputStream  The stream being removed from the StreamIOManager's incoming connections.
     */
    public void disconnect(final PipedInputStream inputStream) {
        this.inputStreams.remove(inputStream);
    }

    /**
     * Callable class used to check the PipedInputStreams for input.
     */
    private class PipedInputStreamChecker implements Callable<BarkPacket> {

        /**
         * Iterates over the stored PipedInputStreams to find a packet to return.  Returns the packet once it is found.
         * @return  A BarkPacket from one of the PipedInputStreams.
         * @throws IOException  if there are issues using the PipedInputStreams.
         */
        @Override
        public BarkPacket call() throws IOException {
            // to ensure that we don't over poll the earlier input streams in the array, we start from a random index.
            int i = new Random().nextInt(inputStreams.size() - 1);

            // iterate over the streams, checking for input.
            while (true) {
                for (; i < inputStreams.size(); i++) {
                    final PipedInputStream inputStream = inputStreams.get(i);

                    // if there is available input, turn it into a BarkPacket and return it.
                    if (inputStream.available() > 0) {
                        final int inputSize = inputStream.available();
                        final byte[] inputContents = inputStream.readNBytes(inputSize);
                        return new BarkPacket(inputContents);
                    }
                }

                // reset i to zero, allowing future loops to start at the beginning of the input stream array.
                i = 0;
            }
        }
    }
}
