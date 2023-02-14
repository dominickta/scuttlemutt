package crypto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * A static helper class for cryptography primatives based on 4096-bit RSA.
 * 
 * The available methods to call are:
 * - KeyPair generateKeyPair()
 * - byte[] encrypt(byte[] plaintext, PublicKey pk)
 * - byte[] decrypt(byte[] ciphertext, PrivateKey pk)
 * - byte[] sign(byte[] message, PrivateKey pk)
 * - bool verify(byte[] signature, byte[] message, PublicKey pk)
 */
public class Crypto {
    public static final KeyPair alice = generateKeyPair();
    public static final KeyPair bob = generateKeyPair();
    public static final KeyPair charlie = generateKeyPair();

    /**
     * Generates a new 4096-bit RSA KeyPair.
     * 
     * @return the new KeyPair
     */
    public static final KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(4096, new SecureRandom());
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
     * Encrypts the byte array payload with the given public key.
     * 
     * @param payload   the byte array to encrypt
     * @param publicKey the public key to encrypt with
     * @return the encrypted (cyphertext) byte array, or empty on error
     */
    public static final byte[] encrypt(byte[] payload, PublicKey publicKey) {
        byte[] result = {};
        try {
            Cipher encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            result = encryptCipher.doFinal(payload);
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
     * Decrypts the byte array payload with the given private key.
     * 
     * @param payload    the byte array to encrypt
     * @param privateKey the private key to encrypt with
     * @return the decrypted (plaintext) byte array, or empty on error
     */
    public static final byte[] decrypt(byte[] payload, PrivateKey privateKey) {
        byte[] result = {};
        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            result = decryptCipher.doFinal(payload);
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
            // This could happen, but shouldn't.
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
