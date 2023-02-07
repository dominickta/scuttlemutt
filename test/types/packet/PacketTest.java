package types.packet;

import org.junit.jupiter.api.Test;
import types.TestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacketTest {

    @Test
    public void testBarkPacketNetworkByteConversion_convertsToBytes_convertsFromBytes_identicalObject() {
        // create a BarkPacket.
        final BarkPacket barkPacket = TestUtils.generateRandomizedBarkPacket();

        // convert the BarkPacket to bytes.
        final byte[] bytes = barkPacket.toNetworkBytes();

        // convert the byte[] back to a BarkPacket.
        final BarkPacket convertedBarkPacket = (BarkPacket) Packet.fromNetworkBytes(bytes);

        // assert that the original BarkPacket is identical to the one we converted.
        assertEquals(barkPacket, convertedBarkPacket);
    }

    @Test
    public void testKeyExchangePacketNetworkByteConversion_convertsToBytes_convertsFromBytes_identicalObject() {
        // create a KeyExchangePacket.
        final KeyExchangePacket kePacket = TestUtils.generateRandomizedKeyExchangePacket();

        // convert the BarkPacket to bytes.
        final byte[] bytes = kePacket.toNetworkBytes();

        // convert the byte[] back to a KeyExchangePacket.
        final KeyExchangePacket convertedKePacket = (KeyExchangePacket) Packet.fromNetworkBytes(bytes);

        // assert that the original BarkPacket is identical to the one we converted.
        assertEquals(kePacket, convertedKePacket);
    }
}