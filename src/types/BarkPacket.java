package types;

import java.util.Arrays;

/**
 * This class represents the packets sent by the IOManager.
 *
 * TODO:  Revise this + maybe add some unit tests when implementing the mesh daemon and we know what the format of the
 *   packets should look like.
 */
public class BarkPacket {
    private final byte[] packetContents;

    /**
     * Constructs the packet.
     * @param packetContents  The contents of the packet.
     */
    public BarkPacket(final byte[] packetContents) {
        this.packetContents = packetContents;
    }

    public byte[] getPacketContents() {
        return Arrays.copyOf(packetContents, packetContents.length);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BarkPacket)) {
            return false;
        }
        return Arrays.equals(this.packetContents, ((BarkPacket) o).getPacketContents());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.packetContents);
    }
}
