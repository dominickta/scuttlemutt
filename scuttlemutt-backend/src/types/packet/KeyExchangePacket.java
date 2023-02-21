package types.packet;

import java.security.Key;

import types.DawgIdentifier;
import types.serialization.SerializationUtils;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import types.serialization.SerializationUtils;

/**
 * This class represents the key-exchange packets sent by the IOManager.
 */
public class KeyExchangePacket extends Packet {
    private final byte[] publicKeyBytes;
    private final byte[] secretKeyBytes;

    private final DawgIdentifier dawgId;

    /**
     * Constructs the packet.
     *
     * @param secretKey The symmetric key being sent by the packet.
     */
    public KeyExchangePacket(final PublicKey publicKey, final SecretKey secretKey, DawgIdentifier dawgId) {
        this.publicKeyBytes = SerializationUtils.serializeKey(publicKey);
        this.secretKeyBytes = SerializationUtils.serializeKey(secretKey);
        this.dawgId = dawgId;
    }

    public PublicKey getPublicKey() {
        return (PublicKey) SerializationUtils.deserializeKey(publicKeyBytes);
    }

    public SecretKey getSecretKey() {
        return (SecretKey) SerializationUtils.deserializeKey(secretKeyBytes);
    }

    public DawgIdentifier getDawgId(){
        return this.dawgId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof KeyExchangePacket)) {
            return false;
        }
        KeyExchangePacket other = (KeyExchangePacket) o;
        boolean samePubKeys = this.getPublicKey().equals(other.getPublicKey());
        boolean sameSecKeys = this.getSecretKey().equals(other.getSecretKey());
        return samePubKeys && sameSecKeys;
    }

    @Override
    public int hashCode() {
        return this.getPublicKey().hashCode() * this.getSecretKey().hashCode();
    }

    @Override
    public String toString() {
        return "public: " + this.publicKeyBytes + " secret: " + this.secretKeyBytes;
    }
}
