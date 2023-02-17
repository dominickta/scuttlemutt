package types.packet;

import java.security.Key;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import types.serialization.SerializationUtils;

/**
 * This class represents the key-exchange packets sent by the IOManager.
 */
public class KeyExchangePacket extends Packet {
    private final String keyType;
    private final byte[] keyBytes;

    /**
     * Constructs the packet.
     *
     * @param secretKey The symmetric key being sent by the packet.
     */
    public KeyExchangePacket(final SecretKey secretKey) {
        this.keyType = "secretKey";
        this.keyBytes = SerializationUtils.serializeKey(secretKey);
    }

    /**
     * Constructs the packet.
     *
     * @param pubKey The asymmetric key being sent by the packet.
     */
    public KeyExchangePacket(final PublicKey pubKey) {
        this.keyType = "publicKey";
        this.keyBytes = SerializationUtils.serializeKey(pubKey);
    }

    public Key getKey() {
        if (this.keyType == "secretKey") {
            return SerializationUtils.deserializeSecretKey(keyBytes);
        } else {
            return SerializationUtils.deserializePublicKey(keyBytes);
        }
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
