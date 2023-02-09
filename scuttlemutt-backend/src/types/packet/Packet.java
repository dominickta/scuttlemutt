package types.packet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Abstract class used by packet object types.
 *
 * Any packet type intended to be sent using an IOManager should extend this abstract class.
 */
public abstract class Packet {
    private static Gson GSON = new GsonBuilder().setLenient().create();

    /**
     * Returns a byte[] containing the contents of the BarkPacket.
     * @return a byte[] containing the contents of the BarkPacket.
     */
    public byte[] toNetworkBytes() {
        // since the packetBarks are contained in a list, we have to serialize them instead of the overall object.
        return GSON.toJson(this).getBytes();
    }

    /**
     * Convert the passed byte[] into the correct packet type.
     * @param receivedBytes  The byte[] containing the bytes to source the packet from.
     * @return  A Packet of the correct type constructed from the receivedBytes.
     */
    public static Packet fromNetworkBytes(final byte[] receivedBytes) {
        // convert the byte[] into a String for easy processing.
        final String packetJson = new String(receivedBytes);

        // if the packet's JSON form contains the substring "packetBarks", it's a BarkPacket.
        if (packetJson.contains("packetBarks")) {
            return GSON.fromJson(packetJson, BarkPacket.class);
        // if the packet's JSON form contains the substring "publicKey", it's a KeyExchangePacket.
        } else if (packetJson.contains("publicKey")) {
            return GSON.fromJson(packetJson, KeyExchangePacket.class);
        // otherwise, we don't know what type the packet is.
        } else {
            throw new PacketException("Attempted to deserialize a packet of an unknown type.\tPacket:  " + packetJson);
        }
    }
}
