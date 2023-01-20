package backend.simulation;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Contains methods associated with setting-up PipedInputStreams + PipedOutputStreams.
 */
public class PipedStreamHelper {

    /**
     * Returns a Pair containing connected PipedInputStream + PipedOutputStream objects.
     *
     * Make sure to use this helper method:  in order to use either of these objects, they must be connected in the
     * first place.  Otherwise, an IOException is thrown during execution.
     *
     * @return  a Pair containing connected PipedInputStream + PipedOutputStream objects.
     */
    public static Pair<PipedInputStream, PipedOutputStream> getPipedStreamPair() {
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
        return buildStreamPair(inputStream, outputStream);
    }

    /**
     * Returns a Pair object containing the passed streams.
     *
     * The left value is the PipedInputStream, whereas the right value is the PipedOutputStream.
     *
     * @param inputStream  The PipedInputStream to be stored in the pair.
     * @param outputStream  The PipedOutputStream to be stored in the pair.
     * @return  a Pair containing the passed streams.
     */
    public static Pair<PipedInputStream, PipedOutputStream> buildStreamPair(final PipedInputStream inputStream,
                                                                            final PipedOutputStream outputStream) {
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
