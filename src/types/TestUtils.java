package types;

import crypto.Crypto;
import org.apache.commons.lang3.RandomStringUtils;
import types.packet.BarkPacket;
import types.packet.KeyExchangePacket;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static types.Bark.MAX_MESSAGE_SIZE;

/**
 * Used by our tests to generate the various object types.
 */
public class TestUtils {
    private static final Random r = new Random();

    public static BarkPacket generateRandomizedBarkPacket() {
        return new BarkPacket(List.of(TestUtils.generateRandomizedBark()));
    }

    public static KeyExchangePacket generateRandomizedKeyExchangePacket() {
        return new KeyExchangePacket(Crypto.generateKeyPair().getPublic());
    }
    
    public static Bark generateRandomizedBark() {
        return new Bark(RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE),
                generateRandomizedDawgIdentifier(),
                generateRandomizedDawgIdentifier(),
                r.nextLong());
    }

    public static DawgIdentifier generateRandomizedDawgIdentifier() {
        return new DawgIdentifier(RandomStringUtils.random(15),
                UUID.randomUUID(),
                Crypto.generateKeyPair().getPublic());
    }

    public static Conversation generateRandomizedConversation() {
        return new Conversation(Collections.singletonList(generateRandomizedDawgIdentifier()));
    }
}
