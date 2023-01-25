package types;

import com.google.gson.Gson;

import java.util.List;

/**
 * This class represents the packets sent by the IOManager.
 */
public class BarkPacket {
    private static final Gson GSON = new Gson();
    public final List<Bark> packetBarks;

    /**
     * Constructs the packet.
     * @param packetBarks  The contents of the packet.
     */
    public BarkPacket(final List<Bark> packetBarks) {
        this.packetBarks = packetBarks;
    }

    /**
     * Returns the Barks contained inside the BarkPacket.
     * @return  The Barks contained inside the BarkPacket.
     */
    public List<Bark> getPacketBarks() {
        return List.copyOf(packetBarks);
    }

    /**
     * Returns a byte[] containing the contents of the BarkPacket.
     * @return a byte[] containing the contents of the BarkPacket.
     */
    public byte[] toNetworkBytes() {
        // since the packetBarks are contained in a list, we have to serialize them instead of the overall object.
        return GSON.toJson(this).getBytes();
    }

    /**
     * Convert the passed byte[] into a BarkPacket.
     * @param receivedBytes  The byte[] containing the bytes to source the packet from.
     * @return  A BarkPacket containing the receivedBytes.
     */
    public static BarkPacket fromNetworkBytes(final byte[] receivedBytes) {
        return GSON.fromJson(new String(receivedBytes), BarkPacket.class);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BarkPacket)) {
            return false;
        }
        return this.getPacketBarks().equals(((BarkPacket) o).getPacketBarks());
    }

    @Override
    public int hashCode() {
        return this.getPacketBarks().hashCode();
    }
}
