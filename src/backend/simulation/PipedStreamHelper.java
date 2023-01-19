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
