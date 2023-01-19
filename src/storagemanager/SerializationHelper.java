package storagemanager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Helper class for serializing/deserializing objects for storage.
 * Used by the StorageManager classes.
 *
 * Derived from:  https://stackoverflow.com/a/69446494
 */
public class SerializationHelper {

    public static String serializeObjectToString(final Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public static Object deserializeStringToObject(final String serializedObject) {
        try {
            byte [] data = Base64.getDecoder().decode(serializedObject);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o  = ois.readObject();
            ois.close();
            return o;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
