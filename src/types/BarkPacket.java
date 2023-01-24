package types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents the packets sent by the IOManager.
 *
 * TODO:  Revise this + maybe add some unit tests when implementing the mesh daemon and we know what the format of the
 *   packets should look like.
 */
public class BarkPacket {
    private final List<Bark> packetBarks;

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
    public List<Bark> getPacketContents() {
        return List.copyOf(packetBarks);
    }

    /**
     * Returns a byte[] containing the contents of the BarkPacket.
     * @return a byte[] containing the contents of the BarkPacket.
     */
    public byte[] toNetworkBytes() {
        // create a byte[] to contain the contents of all the bark packets.
        final byte[] barkPacketBytes = new byte[packetBarks.size() * Bark.PACKET_SIZE];

        // rotate through the Barks, store the bytes in the barkPacketBytes.
        for (int i = 0; i < packetBarks.size(); i++) {
            final byte[] barkBytes = packetBarks.get(i).toNetworkBytes();
            // copy the subarray into the returned array.
            System.arraycopy(barkBytes, 0, barkPacketBytes, i * Bark.PACKET_SIZE, Bark.PACKET_SIZE);
        }

        return barkPacketBytes;
    }

    /**
     * Convert the passed byte[] into a BarkPacket.
     * @param receivedBytes  The byte[] containing the bytes to source the packet from.
     * @return  A BarkPacket containing the receivedBytes.
     */
    public static BarkPacket fromNetworkBytes(final byte[] receivedBytes) {
        final List<Bark> packetBarks = new ArrayList<Bark>();

        // go thru the receivedBytes, create a Bark for every Bark.MESSAGE_SIZE bytes.
        for (int i = 0; i < receivedBytes.length / Bark.PACKET_SIZE; i++) {
            // get an array of bytes containing the byte chunk representing the Bark.
            final byte[] barkBytes = new byte[Bark.PACKET_SIZE];
            System.arraycopy(receivedBytes, i * Bark.PACKET_SIZE, barkBytes, 0, Bark.PACKET_SIZE);

            // create a Bark object, store it in packetBarks.
            packetBarks.add(Bark.fromNetworkBytes(barkBytes));
        }

        // return the BarkPacket.
        return new BarkPacket(packetBarks);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BarkPacket)) {
            return false;
        }
        return Arrays.equals(this.toNetworkBytes(), ((BarkPacket) o).toNetworkBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.toNetworkBytes());
    }
}
