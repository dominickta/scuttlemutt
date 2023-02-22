package types.serialization;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import crypto.Crypto;

public class SerializationUtils {
    private static final Gson GSON = new GsonBuilder().setLenient().create();

    // used to indicate key type in serialized String form.
    private static final byte[] SERIALIZED_SECRETKEY_PREFIX_BYTES = "secretkey:".getBytes();
    private static final byte[] SERIALIZED_PUBLICKEY_PREFIX_BYTES = "publickey:".getBytes();

    private static final byte[] SERIALIZED_PRIVATEKEY_PREFIX_BYTES = "privatekey:".getBytes();
    public static String serializeKeyList(final List<Key> keyList) {
        // create a List to store the JSONs for each serialized Key.
        final List<String> keyJsonList = new ArrayList<String>();

        // stash the JSONs for the Keys in the List.
        for (final Key k : keyList) {
            keyJsonList.add(new String(SerializationUtils.serializeKey(k)));
        }

        // serialize the keyJsonList and return it.
        return GSON.toJson(keyJsonList);
    }

    /**
     * Deserializes the given string into a list of key objects.
     * @param serializedKeyList a json string of a list of keys
     * @return the list of key objects, or null on error.
     */
    public static List<Key> deserializeKeyList(final String serializedKeyList) {
        try {
            // deserialize the serializedKeyList to a List<String>.
            final Type arrayListStringType = new TypeToken<ArrayList<String>>() {}.getType();
            final List<String> keyJsonList = GSON.fromJson(serializedKeyList, arrayListStringType);

            // convert the JSONs to Key objects + store them.
            final List<Key> keyList = new ArrayList<Key>();
            for (final String json : keyJsonList) {
                keyList.add(deserializeKey(json.getBytes(StandardCharsets.UTF_8)));
            }

            // return the Key objects.
            return keyList;
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] serializeKey(final Key k) {
        final byte[] encodedBytes = Base64.getEncoder().encode(k.getEncoded());

        // return the encoded bytes prepended with the key type. this will help with
        // deserialization later.
        if (k instanceof SecretKey) {
            return ArrayUtils.addAll(SERIALIZED_SECRETKEY_PREFIX_BYTES, encodedBytes);
        } else if (k instanceof PublicKey) {
            return ArrayUtils.addAll(SERIALIZED_PUBLICKEY_PREFIX_BYTES, encodedBytes);
        } else if (k instanceof PrivateKey){
            return ArrayUtils.addAll(SERIALIZED_PRIVATEKEY_PREFIX_BYTES, encodedBytes);
        }

        // unsupported key type, throw an exception.
        throw new SerializationException("Tried to serialize an unknown key type!");
    }

    public static Key deserializeKey(final byte[] serializedBytes) {
        // Figure out the type of the Key serialized in the byte[], reassemble + return
        // the Key.
        if (indexOf(serializedBytes, SERIALIZED_SECRETKEY_PREFIX_BYTES) != -1) {
            // trim off the prefix.
            final byte[] keyBytes = Arrays.copyOfRange(serializedBytes,
                    SERIALIZED_SECRETKEY_PREFIX_BYTES.length,
                    serializedBytes.length);

            // get the base64 encoded key.
            final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);
            return new SecretKeySpec(encodedKey, 0, encodedKey.length, Crypto.SYMMETRIC_KEY_TYPE);
        } else if (indexOf(serializedBytes, SERIALIZED_PUBLICKEY_PREFIX_BYTES) != -1) {
            // trim off the prefix.
            final byte[] keyBytes = Arrays.copyOfRange(serializedBytes,
                    SERIALIZED_PUBLICKEY_PREFIX_BYTES.length,
                    serializedBytes.length);

            // get the base64 encoded key.
            final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);

            // obtain and return the PublicKey.
            try {
                final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
                final PublicKey deserializedKey;
                deserializedKey = keyFactory.generatePublic(publicKeySpec);
                return deserializedKey;
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                // if there's an error obtaining the PublicKey spec or the RSA algorithm, an
                // exception occurs.
                throw new RuntimeException(e);
            }
        } else if (indexOf(serializedBytes, SERIALIZED_PRIVATEKEY_PREFIX_BYTES) != -1) {
            // trim off the prefix.
            final byte[] keyBytes = Arrays.copyOfRange(serializedBytes,
                    SERIALIZED_PRIVATEKEY_PREFIX_BYTES.length,
                    serializedBytes.length);

            // get the base64 encoded key.
            final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);

            // obtain and return the PrivateKey.
            try {
                final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedKey);
                final PrivateKey deserializedKey;
                deserializedKey = keyFactory.generatePrivate(publicKeySpec);
                return deserializedKey;
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                // if there's an error obtaining the PublicKey spec or the RSA algorithm, an
                // exception occurs.
                throw new RuntimeException(e);
            }
        }

        // if we've reached this point, we were unable to deserialize the provided
        // byte[] as a Key.
        throw new SerializationException("Tried to deserialize an unknown key type!");
    }

    public static PublicKey deserializePublicKey(final byte[] keyBytes) {
        try {
            final byte[] encodedKey = Base64.getDecoder().decode(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (PublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
        } catch (Exception e) {
            // deserialization failed here
            return null;
        }
    }

    private static int indexOf(byte[] array, byte[] target) {
        // I was having trouble loading the package, so I just stole it instead.
        if (target.length == 0) {
            return 0;
        }

        outer: for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
