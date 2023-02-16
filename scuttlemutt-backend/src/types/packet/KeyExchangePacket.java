package types.packet;

import types.serialization.SerializationUtils;

import java.security.Key;

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

    public SecretKey getKey() {
        return SerializationUtils.deserializeSecretKey(keyBytes);
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
