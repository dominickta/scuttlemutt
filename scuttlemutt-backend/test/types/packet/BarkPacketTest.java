package types.packet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.Bark;
import types.TestUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BarkPacketTest {
    // test variables
    private List<Bark> barkList1;
    private List<Bark> barkList2;

    @BeforeEach
    public void setup() {
        // create the Barks used for testing.
        barkList1 = Stream.generate(TestUtils::generateRandomizedBark)
                .limit(10)
                .collect(Collectors.toList());
        barkList2 = Stream.generate(TestUtils::generateRandomizedBark)
                .limit(10)
                .collect(Collectors.toList());
    }

    @Test
    public void testConstructor_createsObjectSuccessfully() {
        // Successfully create a BarkPacket object.
        final BarkPacket bp = new BarkPacket(barkList1);
    }

    @Test
    public void testGetPacketContents_returnsContents(){
        // Successfully create a BarkPacket object.
        final BarkPacket bp = new BarkPacket(barkList1);

        // Obtain the packet contents.
        final List<Bark> bpContents = bp.getPacketBarks();

        // Assert that the contents are as expected.
        assertEquals(barkList1, bpContents);
    }

    @Test
    public void testEquals_differentObjectsButSameBarks_returnsTrue() {
        // Create two BarkPacket objects with the same List.
        final BarkPacket bp1 = new BarkPacket(barkList1);
        final BarkPacket bp2 = new BarkPacket(barkList1);

        // Verify that the two BarkPacket objects are equal.
        assertEquals(bp1, bp2);
    }

    @Test
    public void testEquals_differentObjectsAndDifferentBytes_returnsFalse() {
        // Create two BarkPacket objects with the different Lists.
        final BarkPacket bp1 = new BarkPacket(barkList1);
        final BarkPacket bp2 = new BarkPacket(barkList2);

        // Verify that the two BarkPacket objects are not equal.
        assertNotEquals(bp1, bp2);
    }
}
