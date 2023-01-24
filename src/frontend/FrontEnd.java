import backend.iomanager.IOManager;
import backend.meshdaemon.MeshDaemon;

/**
 *
 */
public class FrontEnd {
    private final MeshDaemon mesh;
    private final StorageManager storage;

    public FrontEnd(final MeshDaemon mesh, final StorageManager storage) {
        this.mesh = mesh;
        this.storage = storage;
    }
}