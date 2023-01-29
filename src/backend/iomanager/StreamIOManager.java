package backend.iomanager;

import backend.simulation.PipedStreamHelper;
import org.apache.commons.lang3.tuple.Pair;
import types.BarkPacket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * This class represents an InputStream + OutputStream-based I/O manager.
 *
 * This manager can be used to simulate a network via feeding the streams to different devices.
 */
public class StreamIOManager implements IOManager {
    // streams

    // stores the PipedInputStream + PipedOutputSteam pair associated with each device connection.
    private final Map<String, Pair<PipedInputStream, PipedOutputStream>> deviceStreamPairMap;
    // stores the PipedInputStreams to pull data from. (These should be other StreamIOManagers.)
    private final List<PipedInputStream> inputStreams;
    // stores the PipedOutputStreams to write data to (There should be one for each device on the network).
    private final List<PipedOutputStream> outputStreams;

    /**
     * Constructs a StreamIOManager.
     */
    public StreamIOManager() {
        this.deviceStreamPairMap = new HashMap<String, Pair<PipedInputStream, PipedOutputStream>>();
        this.inputStreams = new ArrayList<PipedInputStream>();
        this.outputStreams = new ArrayList<PipedOutputStream>();
    }

    @Override
    public void broadcast(BarkPacket packet) {
        // write the packet to each PipedOutputStream.
        this.outputStreams.forEach(os -> {
            try {
                os.write(packet.toNetworkBytes());
                os.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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


    @Override
    public int numConnections(){
        return inputStreams.size();
    }

    /**
     * Stores the passed PipedInputStream in the StreamIOManager's list of incoming connections.  This is equivalent
     * to creating the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the NetworkSimulation.  To connect StreamIOManagers, please use the
     * methods inside the NetworkSimulation.
     *
     * @param deviceId  The other device (which we are setting up the connection with).
     * @param inputStreamFromDevice  A PipedInputStream feeding bytes from the other device.
     * @param outputStreamToDevice  A PipedOutputStream to send bytes to the other device.
     */
    public void connect(final String deviceId, final PipedInputStream inputStreamFromDevice, final PipedOutputStream outputStreamToDevice) {
        this.deviceStreamPairMap.put(deviceId, PipedStreamHelper.buildStreamPair(inputStreamFromDevice, outputStreamToDevice));
        this.inputStreams.add(inputStreamFromDevice);
        this.outputStreams.add(outputStreamToDevice);
    }

    /**
     * Removes the passed PipedInputStream from the StreamIOManager's incoming connections.  This is equivalent
     * to cutting the connection between the two devices.
     *
     * NOTE:  This method should solely be used by the NetworkSimulation.  To disconnect StreamIOManagers, please use the
     * methods inside the NetworkSimulation.
     *
     * @param deviceId  The device we're disconnecting from.
     */
    public void disconnect(final String deviceId) {
        final Pair<PipedInputStream, PipedOutputStream> deviceStreamPair = this.deviceStreamPairMap.get(deviceId);
        this.inputStreams.remove(deviceStreamPair.getLeft());
        this.outputStreams.remove(deviceStreamPair.getRight());
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
                        return BarkPacket.fromNetworkBytes(inputContents);
                    }
                }

                // reset i to zero, allowing future loops to start at the beginning of the input stream array.
                i = 0;
            }
        }
    }
}
