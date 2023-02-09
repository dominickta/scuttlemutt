package types.packet;

import types.Bark;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the messaging packets sent by the IOManager.
 */
public class BarkPacket extends Packet {
    public final List<Bark> packetBarks;

    /**
     * Constructs the packet.
     * @param packetBarks  The contents of the packet.
     */
    public BarkPacket(final List<Bark> packetBarks) {
        this.packetBarks = packetBarks;
    }

    /**
     * Constructs a copy of another packet.
     * @param barkPacket  The packet to copy.
     */
    public BarkPacket(final BarkPacket barkPacket) {
        this.packetBarks = new ArrayList<>();

        for (Bark bark : barkPacket.packetBarks) {
            this.packetBarks.add(new Bark(bark));
        }
    }

    /**
     * Returns the Barks contained inside the BarkPacket.
     * @return  The Barks contained inside the BarkPacket.
     */
    public List<Bark> getPacketBarks() {
        return List.copyOf(packetBarks);
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
