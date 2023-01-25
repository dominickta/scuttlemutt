package types;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static types.Bark.MAX_MESSAGE_SIZE;

/**
 * Used by our tests to generate the various object types.
 */
public class TestUtils {
    private static final Random r = new Random();

    public static Bark generateRandomizedBark() {
        return new Bark(RandomStringUtils.randomAlphanumeric(MAX_MESSAGE_SIZE),
                generateRandomizedMuttIdentifier(),
                generateRandomizedMuttIdentifier(),
                r.nextLong());
    }

    public static MuttIdentifier generateRandomizedMuttIdentifier() {
        return new MuttIdentifier(RandomStringUtils.random(15),
                UUID.randomUUID(),
                RandomStringUtils.randomAlphabetic(15));
    }

    public static Conversation generateRandomizedConversation() {
        return new Conversation(Collections.singletonList(generateRandomizedMuttIdentifier()));
    }
}
