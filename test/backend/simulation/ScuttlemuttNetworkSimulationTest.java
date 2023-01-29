package backend.simulation;

import backend.iomanager.StreamIOManager;
import scuttlemutt.Scuttlemutt;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.powermock.reflect.Whitebox;
import types.Bark;
import types.BarkPacket;
import types.TestUtils;

import java.io.PipedInputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class ScuttlemuttNetworkSimulationTest {
    // test constants
    public static final int NUM_DEVICES = 3;  // NOTE:  Value must be >= 2 for tests to run successfully.

    // test variables
    private List<String> deviceLabels;
    private ScuttlemuttNetworkSimulation simulation;
    private String message;
    private Scuttlemutt recipient;
    private UUID messageUuid;
    
    @BeforeEach
    public void setup() {
        // generate the device labels.
        deviceLabels = Stream.generate(() -> RandomStringUtils.randomAlphanumeric(15))
                .limit(NUM_DEVICES)
                .collect(Collectors.toList());

        // set a string we can use for message
        this.message = "Hello World";
        // initialize the simulation.
        simulation = new ScuttlemuttNetworkSimulation(deviceLabels);

        final Scuttlemutt sender = simulation.getScuttlemutt(deviceLabels.get(0));

        // get recipient Scuttlemutt
        this.recipient = simulation.getScuttlemutt(deviceLabels.get(1));

        // get recipient Scuttlemutt UUID
        UUID recipientId = recipient.getDawgIdentifier().getUniqueId(); 
        System.out.println("Intended for " + recipientId);
       
        // Ensure network has been fully set up/connected
        boolean connected = false;
        while(!connected){
            for(int i = 0; i < NUM_DEVICES; i++){
                connected = (simulation.getScuttlemutt(deviceLabels.get(1)).numConnections() == NUM_DEVICES - 1);
            }
        }

         // send the message.
        this.messageUuid = sender.sendMessage(message, recipientId);
    }

    @Test
    public void testGetScuttlemutt_sendMessage_verifyCorrectDeviceRecievedCorrectMessage() {
        // get the sender Scuttlemutt
        // verify that intendedRecipient recieved a message
        UUID recipientId = recipient.getDawgIdentifier().getUniqueId(); 
        System.err.println("Looking up Bark " + recipientId);
        assertTimeoutPreemptively(Duration.ofMillis(20000), () -> {
            while(recipient.lookupBark(recipientId) == null);
        });
        Bark recievedMessage = recipient.lookupBark(recipientId);
        assertEquals(messageUuid, recievedMessage.getUniqueId());
    }

    @Test
    public void testGetScuttlemutt_sendMessage_verifyNonRecipientDevicesDidNotStoreMessage() {
        UUID recipientId = recipient.getDawgIdentifier().getUniqueId(); 
        // verify that intendedRecipient recieved a message
        for(int i = 2; i < NUM_DEVICES; i++){
            final Scuttlemutt curDevice = simulation.getScuttlemutt(deviceLabels.get(i));
            Bark recievedMessage = curDevice.lookupBark(recipientId);
            assertEquals(null, recievedMessage);
        }
        
    }


}
