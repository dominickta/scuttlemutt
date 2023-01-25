package backend.meshdaemon;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import backend.iomanager.IOManager;
import types.Bark;
import types.BarkPacket;

/**
 * Controlls outbound messages from the mesh daemon.
 * 
 * This class is runnable and should be run in a separate thread since it might
 * block (it will probably block).
 */
public class MeshOutput implements Runnable {
    // class variables
    private final IOManager ioManager;
    private final BlockingQueue<Bark> queue;

    /**
     * Constructs a new MeshOutput.
     * 
     * @param ioManager The underlying IOManager.
     * @param queue     A queue of barks to send out.
     */
    public MeshOutput(final IOManager ioManager, final BlockingQueue<Bark> queue) {
        this.ioManager = ioManager;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Bark bark = this.queue.take(); // throws InterruptedException
                BarkPacket barkPacket = new BarkPacket(Arrays.asList(bark));
                ioManager.broadcast(barkPacket);
            } catch (InterruptedException e) {
                // interrupted while waiting on take()
                e.printStackTrace();
            }
        }
    }
}
