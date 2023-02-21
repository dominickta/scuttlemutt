package types.packet;

import java.security.Key;

import types.DawgIdentifier;
import types.serialization.SerializationUtils;

import javax.crypto.SecretKey;

/**
 * This class represents the key-exchange packets sent by the IOManager.
 */
public class KeyExchangePacket extends Packet {
    private final byte[] keyBytes;

    private final DawgIdentifier dawgId;

    /**
     * Constructs the packet.
     *
     * @param secretKey The symmetric key being sent by the packet.
     */
    public KeyExchangePacket(final SecretKey secretKey, DawgIdentifier dawgId) {
        this.keyBytes = SerializationUtils.serializeKey(secretKey);
        this.dawgId = dawgId;
    }

    public Key getKey() {
        return SerializationUtils.deserializeKey(keyBytes);
    }

    public DawgIdentifier getDawgId(){
        return this.dawgId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyExchangePacket)) {
            return false;
        }
        return this.getKey().equals(((KeyExchangePacket) o).getKey()) && this.dawgId.equals(((KeyExchangePacket) o).getDawgId());
    }

    @Override
    public int hashCode() {
        return this.getKey().hashCode();
    }
}
