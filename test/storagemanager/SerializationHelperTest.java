package storagemanager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import types.Bark;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationHelperTest {
    private static final Bark b = new Bark(RandomStringUtils.randomAlphanumeric(15));

    @Test
    public void testSerializationAndDeserialization() {
        // serialize the object as a String
        final String serializedObject = SerializationHelper.serializeObjectToString(b);

        // convert the String back to an object
        final Bark deserializedObject = (Bark) SerializationHelper.deserializeStringToObject(serializedObject);

        // assert that the objects are identical
        assertEquals(b, deserializedObject);
    }
}
