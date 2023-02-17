package types;

import static java.lang.Thread.sleep;
import static types.Bark.MAX_MESSAGE_SIZE;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

import crypto.Crypto;
import types.packet.BarkPacket;
import types.packet.KeyExchangePacket;

/**
 * Used by our tests to generate the various object types.
 */
public class TestUtils {
    private static final Random r = new Random();

    public static BarkPacket generateRandomizedBarkPacket() {
        return new BarkPacket(List.of(TestUtils.generateRandomizedBark()));
    }

    public static KeyExchangePacket generateRandomizedKeyExchangePacket() {
        return new KeyExchangePacket(Crypto.DUMMY_SECRETKEY);
    }

    public static Bark generateRandomizedBark() {
        return new Bark(RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE),
                generateRandomizedDawgIdentifier(),
                generateRandomizedDawgIdentifier(),
                r.nextLong(),
                Crypto.ALICE_KEYPAIR.getPublic(),
                Crypto.DUMMY_SECRETKEY);
    }

    public static DawgIdentifier generateRandomizedDawgIdentifier() {
        return new DawgIdentifier(RandomStringUtils.randomAlphanumeric(15), UUID.randomUUID());
    }

    public static Conversation generateRandomizedConversation() {
        return new Conversation(generateRandomizedDawgIdentifier());
    }

    public static void sleepOneSecond() {
        // allow request to complete.
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
