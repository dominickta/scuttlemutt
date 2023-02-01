package backend.iomanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import types.Bark;
import types.BarkPacket;
import types.TestUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Runs tests for the QueueIOManager.
 * 
 * TODO: Add more tests.
 */
public class QueueIOManagerTest {
    /**
     * Tests that a single QueueIOManager can 
     */
    @Test
    public void testSend_singleManager_singlePacket() {
        try {
            QueueIOManager m = new QueueIOManager();

            final String nonManager = "nonManager-" + RandomStringUtils.randomAlphanumeric(15);

            BlockingQueue<BarkPacket> inputQueue = new LinkedBlockingQueue<>();
            BlockingQueue<BarkPacket> outputQueue = new LinkedBlockingQueue<>();

            m.connect(nonManager, inputQueue, outputQueue);

            Bark bark = TestUtils.generateRandomizedBark();
            BarkPacket barkPacket = new BarkPacket(List.of(bark));

            m.send(nonManager, barkPacket);

            assertEquals(barkPacket, outputQueue.poll());

        } catch (IOManagerException e) {
            System.err.println("Unexpected error during test -- " + e);
            throw new RuntimeException(e);
        }
    }
}
