package types;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BarkPacketTest {
    // test variables
    private final byte[] packetContents1 = RandomStringUtils.randomAlphanumeric(15).getBytes();
    private final byte[] packetContents2 = RandomStringUtils.randomAlphanumeric(15).getBytes();

    @Test
    public void testConstructor_createsObjectSuccessfully(){
        // Successfully create a BarkPacket object.
        final BarkPacket bp = new BarkPacket(packetContents1);
    }

    @Test
    public void testGetUserUUIDList_returnsCorrectList(){
        // Successfully create a BarkPacket object.
        final BarkPacket bp = new BarkPacket(packetContents1);

        // Obtain the packet contents.
        final byte[] bpContents = bp.getPacketContents();

        // Assert that the contents are as expected.
        assertArrayEquals(packetContents1, bpContents);
    }

    @Test
    public void testEquals_differentObjectsButSameBytes_returnsTrue(){
        // Create two BarkPacket objects with the same List.
        final BarkPacket bp1 = new BarkPacket(packetContents1);
        final BarkPacket bp2 = new BarkPacket(packetContents1);

        // Verify that the two BarkPacket objects are equal.
        assertEquals(bp1, bp2);
    }

    @Test
    public void testEquals_differentObjectsAndDifferentBytes_returnsFalse(){
        // Create two BarkPacket objects with the same List.
        final BarkPacket bp1 = new BarkPacket(packetContents1);
        final BarkPacket bp2 = new BarkPacket(packetContents2);

        // Verify that the two BarkPacket objects are equal.
        assertNotEquals(bp1, bp2);
    }

}
