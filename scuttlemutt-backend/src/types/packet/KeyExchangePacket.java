package types.packet;

import types.serialization.SerializationUtils;

import java.security.PublicKey;

/**
 * This class represents the key-exchange packets sent by the IOManager.
 */
public class KeyExchangePacket extends Packet {
    private final byte[] publicKeyBytes;

    /**
     * Constructs the packet.
     * @param publicKey  The public key being sent by the packet.
     */
    public KeyExchangePacket(final PublicKey publicKey) {
        this.publicKeyBytes = SerializationUtils.serializeKey(publicKey);
    }

    public PublicKey getPublicKey() {
        return SerializationUtils.deserializeRSAPublicKey(publicKeyBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyExchangePacket)) {
            return false;
        }
        return this.getPublicKey().equals(((KeyExchangePacket) o).getPublicKey());
    }

    @Override
    public int hashCode() {
        return this.getPublicKey().hashCode();
    }
}
