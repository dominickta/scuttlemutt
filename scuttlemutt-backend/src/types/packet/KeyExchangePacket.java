package types.packet;

import java.security.Key;

import types.serialization.SerializationUtils;

import javax.crypto.SecretKey;

/**
 * This class represents the key-exchange packets sent by the IOManager.
 */
public class KeyExchangePacket extends Packet {
    private final byte[] keyBytes;

    /**
     * Constructs the packet.
     *
     * @param secretKey The symmetric key being sent by the packet.
     */
    public KeyExchangePacket(final SecretKey secretKey) {
        this.keyBytes = SerializationUtils.serializeKey(secretKey);
    }

    public Key getKey() {
        return SerializationUtils.deserializeKey(keyBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyExchangePacket)) {
            return false;
        }
        return this.getKey().equals(((KeyExchangePacket) o).getKey());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }
}
