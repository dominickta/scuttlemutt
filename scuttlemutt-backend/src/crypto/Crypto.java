package crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * A static helper class for cryptography primatives based on 4096-bit RSA.
 * 
 * The available methods to call are:
 * - KeyPair generateKeyPair()
 * - KeyPair generateSecretKey()
 * - byte[] encrypt(byte[] plaintext, PublicKey pk)
 * - byte[] decrypt(byte[] ciphertext, PrivateKey pk)
 * - byte[] sign(byte[] message, PrivateKey pk)
 * - bool verify(byte[] signature, byte[] message, PublicKey pk)
 */
public class Crypto {
    public static final String ASYMMETRIC_KEY_TYPE = "RSA";
    public static final String ASYMMETRIC_CIPHER_SPEC = "RSA/ECB/PKCS1Padding";
    public static final String SYMMETRIC_KEY_TYPE = "AES";
    public static final KeyPair ALICE_KEYPAIR = Crypto.generateKeyPair();
    public static final KeyPair BOB_KEYPAIR = Crypto.generateKeyPair();
    public static final SecretKey DUMMY_SECRETKEY = Crypto.generateSecretKey();
    public static final SecretKey OTHER_SECRETKEY = Crypto.generateSecretKey();
    private static final int ASYMMETRIC_KEY_SIZE = 4096;
    private static final int SYMMETRIC_KEY_SIZE = 128; // the size of the symmetric key.
    private static final int MAX_MESSAGE_SIZE = ASYMMETRIC_KEY_SIZE / 8 - 11;

    /**
     * Generates a new 4096-bit RSA KeyPair.
     * 
     * @return the new KeyPair
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(Crypto.ASYMMETRIC_KEY_TYPE);
            generator.initialize(ASYMMETRIC_KEY_SIZE);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            // Will throw if the platform's crypto provider doesn't support the
            // KeyPairGeneratorSpi implementation for the specified algorithm.
            // 4096-bit RSA is common, so this probably won't happen.
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a new AES SecretKey.
     *
     * @return the new SecretKey.
     */
    public static SecretKey generateSecretKey() {
        final KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(Crypto.SYMMETRIC_KEY_TYPE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(SYMMETRIC_KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * Encrypts the byte array payload with the given key.
     * 
     * @param payload the byte array to encrypt
     * @param key     the key to encrypt with
     * @param keyType the type of the Key. Use the String constants defined in this
     *                class for this param.
     * @return the encrypted (ciphertext) byte array, or empty on error
     */
    public static byte[] encrypt(final byte[] payload, final Key key, final String keyType) {
        byte[] result = {};
        try {
            Cipher encryptCipher = Cipher.getInstance(keyType);
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
            result = encryptCipher.doFinal(payload, 0, payload.length);
        } catch (NoSuchAlgorithmException e) {
            // Thrown when a particular cryptographic algorithm is requested
            // but is not available in the environment. (Never: RSA is common)
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // Thrown when a particular padding mechanism is requested but is
            // not available in the environment. (Never: don't request padding?)
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // Invalid Keys: invalid encoding, wrong length, uninitialized, etc.
            // This could happen, but shouldn't. Well, these all shouldn't ;)
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // Thrown when the length of data provided to a block cipher is
            // incorrect, i.e., does not match the block size of the cipher.
            // I don't know if will happen, I think it's automatic.
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // Thrown when a particular padding mechanism is expected for the
            // input data but the data is not padded properly.
            // I don't know if will happen, I think it's automatic.
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Decrypts the byte array payload with the given key.
     * 
     * @param payload the byte array to decrypt
     * @param key     the key to decrypt with
     * @param keyType the type of the Key. Use the String constants defined in this
     *                class for this param.
     * @return the decrypted (plaintext) byte array, or empty on error
     */
    public static byte[] decrypt(final byte[] payload, final Key key, final String keyType) {
        byte[] result = {};
        try {
            System.out.println("MESSAGE DECRYPT");
            System.out.println(payload);
            System.out.println(key.toString());
            Cipher decryptCipher = Cipher.getInstance(keyType);
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
            result = decryptCipher.doFinal(payload);
        } catch (NoSuchAlgorithmException e) {
            // Thrown when a particular cryptographic algorithm is requested
            // but is not available in the environment. (Never: RSA is common)
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            // Thrown when a particular padding mechanism is requested but is
            // not available in the environment. (Never: don't request padding?)
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            // Invalid Keys: invalid encoding, wrong length, uninitialized, etc.
            // This could happen, but shouldn't.
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            // Thrown when the length of data provided to a block cipher is
            // incorrect, i.e., does not match the block size of the cipher.
            // I don't know if will happen, I think it's automatic.
            e.printStackTrace();
            return null;
        } catch (BadPaddingException e) {
            // Thrown when a particular padding mechanism is expected for the
            // input data but the data is not padded properly.
            // I don't know if will happen, I think it's automatic.
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /**
     * Signs the payload with the given private key.
     * 
     * @param payload    The data to sign
     * @param privateKey The private key of the signer
     * @return The signature of the input payload.
     */
    public static final byte[] sign(byte[] payload, PrivateKey privateKey) {
        byte[] result = {};
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(privateKey);
            sig.update(payload);
            result = sig.sign();
        } catch (SignatureException e) {
            // Generic Signature Exception
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // Thrown when a particular cryptographic algorithm is requested
            // but is not available in the environment.
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // Invalid Keys: invalid encoding, wrong length, uninitialized, etc.
            // This could happen, but shouldn't.
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Verifies that the signature is valid for the given payload and public key
     * 
     * @return true if the signature is valid, false otherwise
     */
    public static final boolean verify(byte[] signature, byte[] payload, PublicKey publicKey) {
        boolean result = false;
        try {
            Signature publicSignature = Signature.getInstance("SHA256withRSA");
            publicSignature.initVerify(publicKey);
            publicSignature.update(payload);
            result = publicSignature.verify(signature);
        } catch (SignatureException e) {
            // Generic Signature Exception
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // Thrown when a particular cryptographic algorithm is requested
            // but is not available in the environment.
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // Invalid Keys: invalid encoding, wrong length, uninitialized, etc.
            // This could happen, but shouldn't.
            e.printStackTrace();
        }
        return result;
    }
}
